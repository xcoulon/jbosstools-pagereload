/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystr??m
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package org.jboss.tools.web.pagereloader.internal.remote.websocketx;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

public class WebSocketClient {

	private final URI uri;

	private final ClientBootstrap bootstrap;

	private Channel channel = null;

	public WebSocketClient(URI uri) {
		this.uri = uri;
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
	}

	public void open() throws Exception {
		try {
			String protocol = uri.getScheme();
			if (!protocol.equals("ws")) {
				throw new IllegalArgumentException("Unsupported protocol: " + protocol);
			}

			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
			// or V00.
			// If you change it to V00, ping is not supported and remember to
			// change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
			// pipeline.
			final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory().newHandshaker(uri,
					WebSocketVersion.V13, null, false, null);

			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() throws Exception {
					ChannelPipeline pipeline = Channels.pipeline();

					pipeline.addLast("decoder", new HttpResponseDecoder());
					pipeline.addLast("encoder", new HttpRequestEncoder());
					pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker));
					return pipeline;
				}
			});

			// Connect
			System.out.println("WebSocket Client connecting");
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort() != -1 ? uri.getPort() : 9222 ));
			future.syncUninterruptibly();

			channel = future.getChannel();
			handshaker.handshake(channel).syncUninterruptibly();

		} finally {
		}

	}

	// Close
	public void close() {
		try {
			System.out.println("WebSocket Client sending close");
			channel.write(new CloseWebSocketFrame());
			// WebSocketClientHandler will close the connection when the server
			// responds to the CloseWebSocketFrame.
			channel.getCloseFuture().awaitUninterruptibly();
		} finally {
			channel.close();
			bootstrap.releaseExternalResources();
		}
	}

	public void sendMessage(final String message) throws Exception {
		System.out.println("WebSocket Client sending message");
		final ChannelFuture channelFuture = channel.write(new TextWebSocketFrame(message));
		channelFuture.await();
	}
}

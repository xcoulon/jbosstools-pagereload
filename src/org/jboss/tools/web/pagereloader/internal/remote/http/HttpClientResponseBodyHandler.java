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
package org.jboss.tools.web.pagereloader.internal.remote.http;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

public class HttpClientResponseBodyHandler extends SimpleChannelUpstreamHandler {

    private boolean readingChunks;

    private final StringBuilder responseBodyBuilder = new StringBuilder();
    
    public String getResponse() {
    	return responseBodyBuilder.toString();
    }
    
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

        // Log all channel state changes.
        if (e instanceof ChannelStateEvent) {
            System.err.println("Channel state changed: " + e);
        }

        super.handleUpstream(ctx, e);
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();
            if (response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                	System.out.println(content.toString(CharsetUtil.UTF_8));
                    responseBodyBuilder.append(content.toString(CharsetUtil.UTF_8));
                    // let's close the channel now
                    e.getChannel().close();
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                // let's close the channel now
                e.getChannel().close();
            } else {
                final String content = chunk.getContent().toString(CharsetUtil.UTF_8);
                System.out.println(content);
				responseBodyBuilder.append(content);
            }
        }
    }
}

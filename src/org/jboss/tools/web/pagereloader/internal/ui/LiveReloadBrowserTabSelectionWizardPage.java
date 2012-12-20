/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xavier Coulon - Initial API and implementation 
 ******************************************************************************/

package org.jboss.tools.web.pagereloader.internal.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.web.pagereloader.JBossWebPageReloaderPlugin;
import org.jboss.tools.web.pagereloader.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadBrowserTabSelectionWizardPage extends WizardPage {

	private final LiveReloadBrowserTabSelectionWizardModel wizardModel;
	private final DataBindingContext dbc;
	private TableViewer tableViewer;
	private Text browserUrlText;

	protected LiveReloadBrowserTabSelectionWizardPage(final LiveReloadBrowserTabSelectionWizardModel wizardModel,
			final LiveReloadBrowserTabSelectionWizard wizard) {
		super("LiveReload Browser Tab Selection", "LiveReload Browser Tab Selection", JBossWebPageReloaderPlugin
				.getDefault().getImageDescriptor("web-wiz-banner"));
		super.setDescription("Select the browser tab on which you want to enable live reload.");
		super.setTitle("LiveReload Browser Tab Selection");
		this.wizardModel = wizardModel;
		this.dbc = new DataBindingContext();
	}

	@Override
	public boolean isPageComplete() {
		return wizardModel.hasSelection();
	}

	@Override
	public void createControl(Composite parent) {
		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(parent);
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

		// Browser URL
		Composite browserUrlContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().span(1, 1).align(SWT.FILL, SWT.CENTER).grab(true, true)
				.applyTo(browserUrlContainer);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(browserUrlContainer);
		Label browserUrlLabel = new Label(browserUrlContainer, SWT.NONE);
		browserUrlLabel.setText("Browser Remote Debbuging URL:");
		GridDataFactory.fillDefaults().span(1, 1).align(SWT.LEFT, SWT.CENTER).grab(false, false)
				.applyTo(browserUrlLabel);
		browserUrlText = new Text(browserUrlContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(browserUrlText);
		final Button refreshButton = new Button(browserUrlContainer, SWT.PUSH);
		refreshButton.setText("Refresh");
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(refreshButton);
		refreshButton.addSelectionListener(onLoadTabs());

		// text binding
		final IObservableValue browserUrlModelObservable = BeanProperties.value(
				LiveReloadBrowserTabSelectionWizardModel.PROPERTY_BROWSER_URL).observe(wizardModel);
		final IObservableValue browserUrlTextObservable = WidgetProperties.text(SWT.Modify).observe(browserUrlText);
		dbc.bindValue(browserUrlTextObservable, browserUrlModelObservable);

		// let's put focus here
		browserUrlText.forceFocus();

		// Table with available tabs
		createTable(container, dbc);
		tableViewer.addSelectionChangedListener(onBrowserTabSelection());
		IObservableValue selectedBrowserTabModelObservable =
				BeanProperties.value(LiveReloadBrowserTabSelectionWizardModel.PROPERTY_SELECTED_BROWSER_TAB)
						.observe(wizardModel);
		selectedBrowserTabModelObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				final BrowserTab selectedBrowserTab = wizardModel.getSelectedBrowserTab();
				if(selectedBrowserTab != null) {
					setPageComplete(true);
				}
			}
		});
		// load list after controls have been created
		loadTabs();
	}

	private ISelectionChangedListener onBrowserTabSelection() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSelection() instanceof IStructuredSelection) {
					BrowserTab selectedTab = (BrowserTab) ((IStructuredSelection)event.getSelection()).getFirstElement();
					wizardModel.setSelectedBrowserTab(selectedTab);
				}
			}
		};
	}

	private SelectionListener onLoadTabs() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				loadTabs();
			}
		};
	}

	protected void createTable(Composite parent, DataBindingContext dbc) {
		Group browserTabsGroup = new Group(parent, SWT.NONE);
		browserTabsGroup.setText("Available Tabs");

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(browserTabsGroup);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(browserTabsGroup);

		Composite tableComposite = new Composite(browserTabsGroup, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tableComposite);
		this.tableViewer =  new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL
		        | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(new ArrayContentProvider());
		// Layout the viewer
	    final Table table = tableViewer.getTable();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		createTableColumn("Title", 1, new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				BrowserTab tab = (BrowserTab) cell.getElement();
				cell.setText(tab.getTitle());
			}
		}, tableViewer, tableColumnLayout);

		createTableColumn("URL", 2, new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				BrowserTab tab = (BrowserTab) cell.getElement();
				cell.setText(tab.getUrl());
			}
		}, tableViewer, tableColumnLayout);
	}

	private void createTableColumn(String name, int weight, CellLabelProvider cellLabelProvider, TableViewer viewer,
			TableColumnLayout layout) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(name);
		column.setLabelProvider(cellLabelProvider);
		layout.setColumnData(column.getColumn(), new ColumnWeightData(weight, true));
	}

	private void loadTabs() {
		try {
			wizardModel.loadTabs();
			tableViewer.setInput(wizardModel.getBrowserTabs());
		} catch (Exception t) {
			Logger.error("Failed to load Browser tabs", t);
		}
	}
	
}

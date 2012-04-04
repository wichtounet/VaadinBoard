package osf.demos;

import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import osf.demos.MovieEditor.EditorSavedEvent;
import osf.demos.MovieEditor.EditorSavedListener;
import osf.demos.model.Movie;

public class MyVaadinApplication extends Application {
    private static String[] visibleCols = new String[] { "lastName", "firstName", "company" };

    private Table contactList = new Table();
    private Form contactEditor = new Form();
    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();
    private Button contactRemovalButton;
    
    JPAContainer<Movie> container = JPAContainerFactory.make(Movie.class, "VaadinPersistenceUnit");

    @Override
    public void init() {
        initLayout();
        initContactAddRemoveButtons();
        initAddressList();
        initFilteringControls();
    }

    private void initLayout() {
        VerticalLayout left = new VerticalLayout();
        setMainWindow(new Window("Address Book", left));
        
        left.setSizeFull();
        left.addComponent(contactList);
        contactList.setSizeFull();
        contactList.setColumnReorderingAllowed(true);
        left.setExpandRatio(contactList, 1);
        left.addComponent(bottomLeftCorner);
    }

    private void initContactAddRemoveButtons() {
        bottomLeftCorner.addComponent(new Button("new",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        final BeanItem<Movie> newPersonItem = new BeanItem<Movie>(new Movie());
                        MovieEditor personEditor = new MovieEditor(newPersonItem);
                        personEditor.addListener(new EditorSavedListener() {
                            @Override
                            public void editorSaved(EditorSavedEvent event) {
                                container.addEntity(newPersonItem.getBean());
                            }
                        });
                        getMainWindow().addWindow(personEditor);
                    }
                }));
        
        bottomLeftCorner.addComponent(new Button("edit",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if(contactList.getValue() != null){
                            final MovieEditor personEditor = new MovieEditor(contactList.getItem(contactList.getValue()));
                            getMainWindow().addWindow(personEditor);
                        }
                    }
                }));
        
        contactRemovalButton = new Button("delete", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if(contactList.getValue() != null){
                    container.removeItem(contactList.getValue());
                }
            }
        });        
        bottomLeftCorner.addComponent(contactRemovalButton);
    }

    private void initAddressList() {        
        contactList.setContainerDataSource(container);
        contactList.setVisibleColumns(visibleCols);
        contactList.setSelectable(true);
        contactList.setImmediate(true);
    }

    private void initFilteringControls() {
        for (final String pn : visibleCols) {
            final TextField sf = new TextField();
            bottomLeftCorner.addComponent(sf);
            sf.setWidth("100%");
            sf.setInputPrompt(pn);
            sf.setImmediate(true);
            bottomLeftCorner.setExpandRatio(sf, 1);
            sf.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    container.removeContainerFilters(pn);
                    
                    if (sf.toString().length() > 0 && !pn.equals(sf.toString())) {
                        container.addContainerFilter(pn, sf.toString(), true, false);
                    }
                    
                    getMainWindow().showNotification("" + container.size() + " matches found");
                }
            });
        }
    }
}
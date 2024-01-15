package org.vaadin.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Route("grid")
public class GridColumnFiltering extends Div {

    /**
     * CustomGrid to allow access to ComponentEventBus
     *
     * @param <T>
     */
    public static class CustomGrid<T> extends Grid<T> {

        public CustomGrid(Class<T> personClass, boolean b) {
            super(personClass, b);
        }

        public ComponentEventBus getEventBusX() {
            return super.getEventBus();
        }
    }

    private Person draggedItem;

    public GridColumnFiltering() {
        // use CustomGrid instead of standard
        CustomGrid<Person> grid = new CustomGrid<Person>(Person.class, false);
        Grid.Column<Person> nameColumn = grid.addColumn(createPersonRenderer())
                .setWidth("230px").setFlexGrow(0);
        Grid.Column<Person> emailColumn = grid.addColumn(Person::getEmail);
        Grid.Column<Person> professionColumn = grid
                .addColumn(createProfessionRenderer());

        List<Person> people = getPeople();
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(people);
        grid.setDataProvider(dataProvider);
        PersonFilter personFilter = new PersonFilter(dataProvider);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(nameColumn).setComponent(
                createFilterHeader("Name", personFilter::setFullName));
        headerRow.getCell(emailColumn).setComponent(
                createFilterHeader("Email", personFilter::setEmail));
        headerRow.getCell(professionColumn).setComponent(
                createFilterHeader("Profession", personFilter::setProfession));

        grid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
        grid.setRowsDraggable(true);

        grid.addDragStartListener(
                e -> draggedItem = e.getDraggedItems().get(0));

        ComponentEventListener<CustomGridDropEvent<Person>> gridDropEventComponentEventListener = e -> {
            Person targetPerson = (Person) e.getDropTargetItem().orElse(null);
            GridDropLocation dropLocation = e.getDropLocation();

            boolean personWasDroppedOntoItself = draggedItem
                    .equals(targetPerson);

            if (targetPerson == null || personWasDroppedOntoItself)
                return;

            people.remove(draggedItem);

            if (dropLocation == GridDropLocation.BELOW) {
                people.add(people.indexOf(targetPerson) + 1, draggedItem);
            } else {
                people.add(people.indexOf(targetPerson), draggedItem);
            }
            dataProvider.refreshAll();
        };

        // listen CustomGridDropEvent instead of standard GridDropEvent
        grid.getEventBusX().addListener(CustomGridDropEvent.class,
                (ComponentEventListener) gridDropEventComponentEventListener);


        grid.addDragEndListener(e -> draggedItem = null);

        add(grid);
    }

    private ComponentRenderer<TextField, Person> createProfessionRenderer() {
        return new ComponentRenderer<>(e -> new TextField(), (tf, person) -> {
            tf.setVisible(true);
            return tf;
        });
    }

    private List<Person> getPeople() {
        List<Person> personList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Person p = new Person("", "foo" + i, "test" + i + "@test.com", "");
            personList.add(p);
        }
        return personList;
    }

    private static Component createFilterHeader(String labelText,
                                                Consumer<String> filterChangeConsumer) {
        Label label = new Label(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-xs)");
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class PersonFilter {
        private final ListDataProvider<Person> dataProvider;

        private String fullName;
        private String email;
        private String profession;

        public PersonFilter(ListDataProvider<Person> dataProvider) {
            this.dataProvider = dataProvider;
            this.dataProvider.addFilter(this::test);
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
            this.dataProvider.refreshAll();
        }

        public void setEmail(String email) {
            this.email = email;
            this.dataProvider.refreshAll();
        }

        public void setProfession(String profession) {
            this.profession = profession;
            this.dataProvider.refreshAll();
        }

        public boolean test(Person person) {
            boolean matchesFullName = matches(person.getFullName(), fullName);
            boolean matchesEmail = matches(person.getEmail(), email);
            boolean matchesProfession = matches(person.getProfession(),
                    profession);

            return matchesFullName && matchesEmail && matchesProfession;
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty() || value
                    .toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

    private static TemplateRenderer<Person> createPersonRenderer() {
        return TemplateRenderer.<Person>of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                        + "  <vaadin-avatar img=\"[[item.pictureUrl]]\" name=\"[[item.fullName]]\"></vaadin-avatar>"
                        + "  <span> [[item.fullName]] </span>"
                        + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", Person::getPictureUrl)
                .withProperty("fullName", Person::getFullName);
    }

    public class Person {

        private String pictureUrl;
        private String fullName;
        private String email;
        private String profession;


        public Person(String pictureUrl, String fullName, String email, String profession) {
            this.pictureUrl = pictureUrl;
            this.fullName = fullName;
            this.email = email;
            this.profession = profession;
        }

        public String getPictureUrl() {
            return pictureUrl;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getProfession() {
            return profession;
        }
    }
}
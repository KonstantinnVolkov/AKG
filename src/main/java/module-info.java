module com.bsuir.lab.obj_renderer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires commons.math3;

    opens com.bsuir.lab.obj_renderer to javafx.fxml;
    exports com.bsuir.lab.obj_renderer;
}
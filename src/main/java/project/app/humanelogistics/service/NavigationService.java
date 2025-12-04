package project.app.humanelogistics.service;

import javafx.scene.control.Button;
import java.util.Arrays;
import java.util.List;

public class NavigationService {
    private List<Button> navigationButtons;

    public void registerButtons(Button... buttons) {
        this.navigationButtons = Arrays.asList(buttons);
    }

    public void setActiveButton(Button activeButton) {
        if (navigationButtons == null) return;

        navigationButtons.forEach(btn ->
                btn.getStyleClass().remove("active"));

        activeButton.getStyleClass().add("active");
    }
}
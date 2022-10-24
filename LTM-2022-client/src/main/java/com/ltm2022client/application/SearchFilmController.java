package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONObject;

public class SearchFilmController implements Initializable {
    private static final ObservableList<Film> searchList = FXCollections.observableArrayList();

    @FXML
    private TextField searchTxtField;

    @FXML
    private Button searchBtn;

    @FXML
    private GridPane gridPane;

    @FXML
    private AnchorPane subContentPane;

    @FXML
    private ScrollPane scrollPane;

    private static Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Handler();
    }

    public void Handler() {
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if (searchList.size() != 0){
                        System.out.println(searchList.size());
                        searchList.clear();
                        System.out.println(searchList.size());
                    }

                    if(gridPane.getChildren().size() != 0){
                        gridPane.getChildren().clear();
                    }

                    String seachValue = searchTxtField.getText();
                    MainController.out.write(seachValue);
                    MainController.out.newLine();
                    MainController.out.flush();
                    String line;
                    JSONObject json;
                    while ((line = MainController.in.readLine()) != null) {
                        if (line.equals("done"))
                            break;

                        json = new JSONObject(line);
                        Film film = new Film();
                        film.setActor(json.getString("actors"));
                        film.setPoster(json.getString("filmPoster"));
                        film.setImdb(json.getString("imdb"));
                        film.setDescription(json.getString("filmDescription"));
                        film.setTrailer(json.getString("filmTrailer"));
                        film.setDirector(json.getString("filmDirector"));
                        film.setName(json.getString("filmName"));
                        film.setYear(json.getString("filmYear"));
                        film.setGern(json.getString("filmGern"));
//                        System.out.println(film);
                        searchList.add(film);
                    }

                    loadFilmFromSearchList();
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        });
    }

    public void loadFilmFromSearchList() throws IOException {
        if(searchList.size() > 0){
            primaryStage = (Stage) subContentPane.getScene().getWindow();
            int col = 1;
            int row = 1;
            int max = 3;
            if(primaryStage.isMaximized())
                max = 4;
            for (Film film : searchList) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("film-item.fxml"));
                AnchorPane box = fxmlLoader.load();
                FilmItemCotroller filmItemCotroller = fxmlLoader.getController();
                filmItemCotroller.setValue(film);
                if (col == max) {
                    col = 1;
                    ++row;
                }
                gridPane.add(box, col++, row);
                GridPane.setMargin(box, new Insets(5));

            }
        }
    }
}

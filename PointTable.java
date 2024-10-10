package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class PointTable extends Application {

    private int teams;
    private int[] points, played, won, lost;
    private float[] nrr;
    private String[] names;
    private int[] totalRunsScored, totalBallsFaced, totalRunsConceded, totalBallsBowled;


    private ObservableList<TeamData> teamDataList = FXCollections.observableArrayList();
    private TableView<TeamData> pointsTable;
    private TextField teamCountField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Point Table");

        // Create a main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Input for number of teams
        Label teamCountLabel = new Label("Enter number of teams:");
        teamCountField = new TextField();
        teamCountField.setPrefWidth(100);

        Button setupButton = new Button("Setup Teams");
        setupButton.setOnAction(e -> setupTeams());

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.add(teamCountLabel, 0, 0);
        inputGrid.add(teamCountField, 1, 0);
        inputGrid.add(setupButton, 2, 0);

        Button addMatchButton = new Button("Add Match");
        addMatchButton.setOnAction(e -> addMatch());
        inputGrid.add(addMatchButton, 1, 1);

        // Table for displaying point table
        pointsTable = new TableView<>();
        TableColumn<TeamData, String> teamColumn = new TableColumn<>("Team");
        teamColumn.setCellValueFactory(data -> data.getValue().teamNameProperty());

        TableColumn<TeamData, Integer> playedColumn = new TableColumn<>("Played");
        playedColumn.setCellValueFactory(data -> data.getValue().playedProperty().asObject());

        TableColumn<TeamData, Integer> wonColumn = new TableColumn<>("Won");
        wonColumn.setCellValueFactory(data -> data.getValue().wonProperty().asObject());

        TableColumn<TeamData, Integer> lostColumn = new TableColumn<>("Lost");
        lostColumn.setCellValueFactory(data -> data.getValue().lostProperty().asObject());

        TableColumn<TeamData, Integer> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(data -> data.getValue().pointsProperty().asObject());

        TableColumn<TeamData, Float> nrrColumn = new TableColumn<>("NRR");
        nrrColumn.setCellValueFactory(data -> data.getValue().nrrProperty().asObject());

        pointsTable.getColumns().addAll(teamColumn, playedColumn, wonColumn, lostColumn, pointsColumn, nrrColumn);

        mainLayout.getChildren().addAll(inputGrid, pointsTable);

        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to set up teams
    private void setupTeams() {
        try {
            teams = Integer.parseInt(teamCountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number of teams.");
            return;
        }
        totalRunsScored = new int[teams];
        totalBallsFaced = new int[teams];
        totalRunsConceded = new int[teams];
        totalBallsBowled = new int[teams];

        names = new String[teams];
        points = new int[teams];
        played = new int[teams];
        won = new int[teams];
        lost = new int[teams];
        nrr = new float[teams];

        // Input dialog to get team names
        for (int i = 0; i < teams; i++) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Team Name");
            dialog.setHeaderText("Enter name for Team " + (i + 1));
            dialog.setContentText("Team Name:");

            final int index = i; // Capture the current value of i
            dialog.showAndWait().ifPresent(name -> {
                names[index] = name;
                teamDataList.add(new TeamData(name, 0, 0, 0, 0, 0.0f));
            });
        }

        // Populate table with team data
        pointsTable.setItems(teamDataList);
    }

    // Method to add match details
    private void addMatch() {
        // Create a dialog to input match details
        Dialog<ButtonType> matchDialog = new Dialog<>();
        matchDialog.setTitle("Add Match");

        // Create a grid for match input
        GridPane matchGrid = new GridPane();
        matchGrid.setHgap(10);
        matchGrid.setVgap(10);

        // Team 1 selection
        ComboBox<String> team1ComboBox = new ComboBox<>();
        team1ComboBox.getItems().addAll(names);
        matchGrid.add(new Label("Team 1:"), 0, 0);
        matchGrid.add(team1ComboBox, 1, 0);

        // Team 1 score input
        TextField team1ScoreField = new TextField();
        matchGrid.add(new Label("Team 1 Score:"), 0, 1);
        matchGrid.add(team1ScoreField, 1, 1);

        TextField team1BallsField = new TextField();
        matchGrid.add(new Label("Team 1 balls faced:"), 0, 2);
        matchGrid.add(team1BallsField, 1, 2);

        // Team 2 selection (opponent)
        ComboBox<String> team2ComboBox = new ComboBox<>();
        team2ComboBox.getItems().addAll(names);
        matchGrid.add(new Label("Team 2 (Opponent):"), 0, 3);
        matchGrid.add(team2ComboBox, 1, 3);

        // Team 2 score input
        TextField team2ScoreField = new TextField();
        matchGrid.add(new Label("Team 2 Score:"), 0, 4);
        matchGrid.add(team2ScoreField, 1, 4);

        TextField team2BallsField = new TextField();
        matchGrid.add(new Label("Team 2 balls faced:"), 0, 5);
        matchGrid.add(team2BallsField, 1, 5);

        matchDialog.getDialogPane().setContent(matchGrid);
        matchDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        matchDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Get input values
                int team1Index = team1ComboBox.getSelectionModel().getSelectedIndex();
                int team2Index = team2ComboBox.getSelectionModel().getSelectedIndex();
                int team1Score = Integer.parseInt(team1ScoreField.getText());
                int team2Score = Integer.parseInt(team2ScoreField.getText());
                int team1Balls = Integer.parseInt(team1BallsField.getText());
                int team2Balls = Integer.parseInt(team2BallsField.getText());

                // Update the points table based on match results
                Update(team1Index, team2Index, team1Score, team1Balls, team2Score, team2Balls);
            }
        });
    }

    // Method to update point table after a match
    private void Update(int team1, int team2, int team1Score, int team1Balls, int team2Score, int team2Balls) {
        // Update totals
        totalRunsScored[team1] += team1Score;
        totalBallsFaced[team1] += team1Balls;
        totalRunsConceded[team1] += team2Score;
        totalBallsBowled[team1] += team2Balls;

        totalRunsScored[team2] += team2Score;
        totalBallsFaced[team2] += team2Balls;
        totalRunsConceded[team2] += team1Score;
        totalBallsBowled[team2] += team1Balls;

        // Calculate NRR for both teams
        nrr[team1] = (float)totalRunsScored[team1] / (totalBallsFaced[team1] / 6.0f)
                     - (float)totalRunsConceded[team1] / (totalBallsBowled[team1] / 6.0f);
        
        nrr[team2] = (float)totalRunsScored[team2] / (totalBallsFaced[team2] / 6.0f)
                     - (float)totalRunsConceded[team2] / (totalBallsBowled[team2] / 6.0f);

        // Update match results (win/loss)
        if (team1Score > team2Score) {
            won[team1]++;
            lost[team2]++;
            points[team1] += 2;
        } else {
            won[team2]++;
            lost[team1]++;
            points[team2] += 2;
        }

        played[team1]++;
        played[team2]++;

        refreshTable();
    }


    // Refresh the table with updated points and NRR
    private void refreshTable() {
        teamDataList.clear();
        for (int i = 0; i < teams; i++) {
            teamDataList.add(new TeamData(names[i], played[i], won[i], lost[i], points[i], nrr[i]));
        }

        // Sort the table based on points and NRR
        teamDataList.sort(Comparator.comparingInt(TeamData::getPoints)
                .thenComparing(TeamData::getNrr).reversed());
    }

    // Utility to show alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class representing team data
    public static class TeamData {
        private final javafx.beans.property.SimpleStringProperty teamName;
        private final javafx.beans.property.SimpleIntegerProperty played;
        private final javafx.beans.property.SimpleIntegerProperty won;
        private final javafx.beans.property.SimpleIntegerProperty lost;
        private final javafx.beans.property.SimpleIntegerProperty points;
        private final javafx.beans.property.SimpleFloatProperty nrr;

        public TeamData(String teamName, int played, int won, int lost, int points, float nrr) {
            this.teamName = new javafx.beans.property.SimpleStringProperty(teamName);
            this.played = new javafx.beans.property.SimpleIntegerProperty(played);
            this.won = new javafx.beans.property.SimpleIntegerProperty(won);
            this.lost = new javafx.beans.property.SimpleIntegerProperty(lost);
            this.points = new javafx.beans.property.SimpleIntegerProperty(points);
            this.nrr = new javafx.beans.property.SimpleFloatProperty(nrr);
        }

        public String getTeamName() {
            return teamName.get();
        }

        public javafx.beans.property.StringProperty teamNameProperty() {
            return teamName;
        }

        public int getPlayed() {
            return played.get();
        }

        public javafx.beans.property.IntegerProperty playedProperty() {
            return played;
        }

        public int getWon() {
            return won.get();
        }

        public javafx.beans.property.IntegerProperty wonProperty() {
            return won;
        }

        public int getLost() {
            return lost.get();
        }

        public javafx.beans.property.IntegerProperty lostProperty() {
            return lost;
        }

        public int getPoints() {
            return points.get();
        }

        public javafx.beans.property.IntegerProperty pointsProperty() {
            return points;
        }

        public float getNrr() {
            return nrr.get();
        }

        public javafx.beans.property.FloatProperty nrrProperty() {
            return nrr;
        }
    }
}

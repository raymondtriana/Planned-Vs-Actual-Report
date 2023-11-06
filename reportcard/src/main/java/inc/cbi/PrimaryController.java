package inc.cbi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.layout.VBox;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class PrimaryController {
    private HashMap<String,HashMap<String,driver>> driverMap = new HashMap<String,HashMap<String,driver>>();//date then routeID
    //Requires routeID to be accurate on logs by the time of pretrip
    //Change to a hashmap of dates to driverID once Pedigree adds driverID to the planned v actual report
    //CHECK IF INDEXED BY ID ADDING ID TO HOS REPORT MARCH
    private File plannedActualFile=null;
    private File hosFile=null;
    @FXML
    private VBox leftPanel;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TextArea textArea;
    @FXML
    private Button runReportButton;

    /**
     * Callback: Toggles collapse and expand of side panel.
     */
    @FXML
    public void onToggleSidePanel(){
        leftPanel.setVisible(!leftPanel.isVisible());
        if(leftPanel.isVisible()==false)
            leftPanel.setPrefSize(0.0, leftPanel.getPrefHeight());
        else
            leftPanel.setPrefSize(135.0, leftPanel.getPrefHeight());
    }

    /**
     * Callback: Acquires pointer to Planned V Actual file.
     */
    @FXML
    private void onImportPlannedActual(){
        //File Chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the Planned vs. Actual Jobs by Driver Report");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Comma Separated Variable", "*.CSV")
        );

        //File
        File chosenFile = fileChooser.showOpenDialog(App.getCurrentStage());        
        if(chosenFile!=null){
            plannedActualFile = chosenFile;
            printlnToConsole("THE PLANNED VS ACTUAL FILE IS: "+ plannedActualFile.getPath());
        }
    }

    /**
     * Callback: Acquires pointer to HoSRecords file.
     */
    @FXML
    private void onImportHOS(){
        //FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the HOS Records Timecard Export Report");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Comma Separated Variable", "*.CSV")
        );

        //File
        File chosenFile = fileChooser.showOpenDialog(App.getCurrentStage());
        if(chosenFile!=null){
            hosFile = chosenFile;
            printlnToConsole("THE HOS FILE IS: "+hosFile.getPath());
        }
    }

    /**
     * Callback: Parses both reports and prints results to TreeView node.
     */
    @FXML
    private void onRunReport(){
        runReportButton.setDisable(true);//disable button
        clearConsole();
        printlnToConsole("RUNNING REPORT...");
        try {
            parsePlannedActual();
            parsehosReport();
        } catch (CsvValidationException e) {
            printlnToConsole("CsvValidationException in ParseHOSReport()");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            printlnToConsole("IOException in ParseHOSReport()");
            e.printStackTrace();
            return;
        }finally{
            runReportButton.setDisable(false);
        }
        calculateElapsedTime();
        printToTreeView();
    }

    /**
     * Callback: Opens a popup to save the final report as a CSV.
     */
    @FXML
    private void onExportReport(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a location to save the report");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Comma Separated Variable", "*.csv")
        );
        File savFile = fileChooser.showSaveDialog(App.getCurrentStage());
        printlnToConsole("PATH TO EXPORTED CSV: "+savFile.getAbsolutePath());
        try {
            savFile.createNewFile();
            FileWriter fileWriter = new FileWriter(savFile);
            fileWriter.write("Date,DriverID,Name,RouteID,PlannedStart,PlannedEnd,ElapsedPlannedTime,ActualStart,ActualEnd,ElapsedActualTime,TimeAhead\n");
            for(String dateKey : driverMap.keySet()){
                for(String driverKey : driverMap.get(dateKey).keySet()){
                    driver cacheDriver = driverMap.get(dateKey).get(driverKey);
                    fileWriter.write( cacheDriver.printToCSV() );
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loops through the driver matrix and calculates the elapsed time.
     */
    private void calculateElapsedTime(){
        for(String dateKey : driverMap.keySet()){//per date
            for(String driverKey : driverMap.get(dateKey).keySet()){//per driver
                driverMap.get(dateKey).get(driverKey).calculateElapsedPlannedTime();
                driverMap.get(dateKey).get(driverKey).calculateElapsedActualTime();
                driverMap.get(dateKey).get(driverKey).calculateTimeAhead();
            }
        }
    }

    /**
     * Print the matrix of data from drivers onto the TreeView
     */
    private void printToTreeView(){
        TreeItem<String> RootItem = new TreeItem<String>("Driver Report");
        for(String dateKey : driverMap.keySet()){
            TreeItem<String> branchItem = new TreeItem<String>(dateKey.toString());
            for(String driverKey: driverMap.get(dateKey).keySet()){
                TreeItem<String> leafDriver = new TreeItem<String>(driverMap.get(dateKey).get(driverKey).getName());
                    TreeItem<String> driverIDItem = new TreeItem<String>("ID: "+driverMap.get(dateKey).get(driverKey).getID());
                    leafDriver.getChildren().add(driverIDItem);
                    if(driverMap.get(dateKey).get(driverKey).getPlannedStart()!=null){
                        TreeItem<String> driverPlannedStart = new TreeItem<String>("PlannedStart: "+driverMap.get(dateKey).get(driverKey).getPlannedStart().toString());
                        leafDriver.getChildren().add(driverPlannedStart);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getPlannedEnd()!=null){
                        TreeItem<String> driverPlannedEnd = new TreeItem<String>("PlannedEnd: "+driverMap.get(dateKey).get(driverKey).getPlannedEnd().toString());
                        leafDriver.getChildren().add(driverPlannedEnd);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getElapsedPlannedTime()!=null){
                        TreeItem<String> driverElapsedPlannedTime = new TreeItem<String>("Elapsed Planned Time: "+driverMap.get(dateKey).get(driverKey).getElapsedPlannedTime().toString());
                        leafDriver.getChildren().add(driverElapsedPlannedTime);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getActualStart()!=null){
                        TreeItem<String> driverActualStart = new TreeItem<String>("ActualStart: "+driverMap.get(dateKey).get(driverKey).getActualStart().toString());
                        leafDriver.getChildren().add(driverActualStart);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getActualEnd()!=null){
                        TreeItem<String> driverActualEnd = new TreeItem<String>("ActualEnd: "+driverMap.get(dateKey).get(driverKey).getActualEnd().toString());
                        leafDriver.getChildren().add(driverActualEnd);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getElapsedActualTime()!=null){
                        TreeItem<String> driverElapsedActualTime = new TreeItem<String>("Elapsed Actual Time: "+driverMap.get(dateKey).get(driverKey).getElapsedActualTime().toString());
                        leafDriver.getChildren().add(driverElapsedActualTime);
                    }
                    if(driverMap.get(dateKey).get(driverKey).getTimeAhead()!=null){
                        TreeItem<String> driverTimeAhead = new TreeItem<String>("Time Ahead: "+driverMap.get(dateKey).get(driverKey).getTimeAhead());
                        leafDriver.getChildren().add(driverTimeAhead);
                    }

                branchItem.getChildren().add(leafDriver);
            }
            RootItem.getChildren().add(branchItem);
        }

        treeView.setRoot(RootItem);
    }
    /**
     * Parses the Planned V Actual report from Pedigree. Extracts: new drivers, route dates, route IDs, planned start, and planned end.
     * @throws CsvValidationException
     * @throws IOException
     */
    private void parsePlannedActual() throws CsvValidationException, IOException{
        if(plannedActualFile==null){
            printlnToConsole("PLANNED ACTUAL FILE COULD NOT BE OPENED!!!");
            return;
        }
        
        printlnToConsole("PARSING PLANNED ACTUAL");

        //FILE WAS CHOSEN TO OPEN
        CSVReader csvReader = new CSVReader(new FileReader(plannedActualFile));
        String [] values = null;
        String driverID = "";
        String routedate = null;
        Date plannedStart = null;
        Date plannedEnd = null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        while((values = csvReader.readNext())!=null){//READ TILL EOF

            try {

                driverID = values[2];
                //SET DATES
                plannedStart = timeFormat.parse(values[9]);
                plannedEnd = timeFormat.parse(values[15]);
                routedate = values[4].trim();
                String [] shortform = routedate.split("/");//Required because plannedvsactual has MM/dd/yyyy and hos has MM/dd/yy
                routedate = shortform[0]+"/"+shortform[1]+"/"+shortform[2].substring(shortform[2].length()-2);

                driver newDriver = new driver(values[1],routedate,plannedStart,plannedEnd);//instantiate new driver
    
                driverMap.putIfAbsent(routedate, new HashMap<String,driver>());//add missing date to map
                driverMap.get(routedate).putIfAbsent(driverID, newDriver);//add missing driver to map keyed by routeID
                Date oldPlannedStart = driverMap.get(routedate).get(driverID).getPlannedStart();
                if(oldPlannedStart.after(plannedStart)){
                    driverMap.get(routedate).get(driverID).setPlannedStart(plannedStart);
                }
                Date oldPlannedEnd = driverMap.get(routedate).get(driverID).getPlannedEnd();
                if(oldPlannedEnd.before(plannedEnd)){
                    driverMap.get(routedate).get(driverID).setPlannedEnd(plannedEnd);
                }

            } catch (Exception e) {
                continue;
            }
                
        }
        csvReader.close();

        printlnToConsole("PLANNED VS ACTUAL PARSED");
    }
    /**
     * Parses the HoS Records report cached in the hosFile pointer. extracts actual start time, actual end time, and route ID information.
     * @throws CsvValidationException
     * @throws IOException
     */
    private void parsehosReport() throws CsvValidationException, IOException{
        if(hosFile==null){
            printlnToConsole("HOS REPORT COULD NOT BE OPENED!!!");
            return;
        }
        printlnToConsole("PARSING HOS REPORT");

        String status = "";
        /*STORAGE */
        String [] values = null;
        String driverID = "";
        String routedate = null;
        Date startTime = null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        int preTripOdometer=0;
        String peek[] = null;
        CSVReader csvReader;
        csvReader = new CSVReader(new FileReader(hosFile));
        while((values = csvReader.readNext())!=null){//READ TILL EOF
            try {
                if(csvReader.peek()!=null)
                    peek = csvReader.peek();
                if(values[8].contains("Pre"))
                    preTripOdometer = Integer.parseInt(values[5]);
                //routedate = values[0].trim();
                routedate = convertToShortDate(values[0].trim());
                startTime = timeFormat.parse(values[9]);
                driverID = values[1];
                status = values[7];
                
                driverMap.get(routedate).get(driverID).setID(values[1]);
                    
                if(status.contains("Off")){
                    preTripOdometer=0;
                    driverMap.get(routedate).get(driverID).initializeActualEnd(startTime);
                    driverMap.get(routedate).get(driverID).setRouteID(values[6]);
                }
                if(status.contains("Off")!=true && peek!=null){
                    if(preTripOdometer!=0 && peek!=null && preTripOdometer!=Integer.parseInt(peek[5])/*preTripOdometer!=Integer.parseInt(values[5])*/){
                        driverMap.get(routedate).get(driverID).initializeAcualStart(timeFormat.parse(values[9]));
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        csvReader.close();//close file

        printlnToConsole("HOS REPORT PARSED");

    }

    /**
     * Prints a string to the on screen console without a new line character.
     * @param newPString The string to be concatenated to the console.
     */
    private void printToConsole(String newPString){
        textArea.setText(textArea.getText()+newPString);
    }

    private void printlnToConsole(String newPString){
        textArea.setText(textArea.getText()+newPString+"\n");
    }

    private void clearConsole(){
        textArea.clear();
    }

    public void initialize(){

    }

    private String convertToShortDate(String longDate){
        String [] temp = longDate.split("/");
        String shortDate = temp[0] + "/" + temp[1] + "/" + temp[2].substring(temp[2].length()-2);
        return shortDate;
    }
}
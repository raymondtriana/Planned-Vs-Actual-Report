package inc.cbi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.usermodel.DateUtil;


public class driver {
    private String name ="";//BOTH
    private String ID = "";//HOS//technically both but hos has qoutation between names
    private String date = null;//BOTH
    private String routeID="";
    private Date plannedStart = null;//PLANNED ACTUAL REPORT
    private Date plannedEnd = null;//PLANNED ACTUAL REPORT
    private String elapsedPlannedTime = null;
    private String tractor = "";//HOS
    private String trailer = "";//HOS
    private Date actualStart = null;//HOS
    private Date actualEnd = null;//HOS
    private String elapsedActualTime = null;
    private String timeAhead = null;

    public driver(String name,String date,Date plannedStart,Date plannedEnd){
        this.name = name;
        this.date = date;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setID(String ID){//change to a regex search
        if(ID.contains("cbi"))
            this.ID = ID.split("cbi")[1];
        else
            if(ID.contains("Cbi"))
                this.ID = ID.split("Cbi")[1];
        else
            this.ID = ID;
    }
    public void setDate(String date){
        this.date = date;
    }
    public void setPlannedStart(Date plannedStart){
        this.plannedStart = plannedStart;
    }
    public void setPlannedEnd(Date plannedEnd){
        this.plannedEnd = plannedEnd;
    }
    public void setTractor(String tractor){
        this.tractor = tractor;
    }
    public void setTrailer(String trailer){
        this.trailer = trailer;
    }
    public void setActualStart(Date actualStart){
        this.actualStart = actualStart;
    }
    public void initializeAcualStart(Date actualStart){
        if(this.actualStart==null)
            this.actualStart = actualStart;
    }
    public void setActualEnd(Date actualEnd){
        this.actualEnd = actualEnd;
    }
    public void initializeActualEnd(Date actualEnd){
        if(this.actualEnd == null || this.actualEnd.before(actualEnd))
            this.actualEnd = actualEnd;
    }
    public void setRouteID(String newID){
        this.routeID = newID;
    }
    public String getName(){
        return this.name;
    }
    public String getID(){
        return this.ID;
    }
    public String getTractor(){
        return this.tractor;
    }
    public String getTrailer(){
        return this.trailer;
    }
    public String getRouteID(){
        return this.routeID;
    }
    public Date getPlannedStart(){
        return this.plannedStart;
    }
    public long getPlannedStartLong(){
        if(this.plannedStart!=null)
            return this.plannedStart.getTime();
        else
            return 0;
    }
    public double getPlannedStartExcel(){
        if(this.plannedStart!=null){
            return DateUtil.getExcelDate(this.plannedStart);
        }else{
            return 0;
        }
    }
    public Date getPlannedEnd(){
        return this.plannedEnd;
    }
    public long getPlannedEndLong(){
        if(this.plannedEnd!=null)
            return this.plannedEnd.getTime();
        else
            return 0;
    }
    public double getPlannedEndExcel(){
        if(this.plannedEnd!=null){
            return DateUtil.getExcelDate(this.plannedEnd);
        }else{
            return 0;
        }
    }
    public Date getActualStart(){
        return this.actualStart;
    }
    public long getActualStartLong(){
        if(this.actualStart!=null)
            return this.actualStart.getTime();
        else
            return 0;
    }
    public double getActualStartExcel(){
        if(this.actualStart!=null)
            return DateUtil.getExcelDate(this.actualStart);
        else
            return 0;
    }
    public Date getActualEnd(){
        return this.actualEnd;
    }
    public long getActualEndLong(){
        if(this.actualEnd!=null)
            return this.actualEnd.getTime();
        else
            return 0;
    }
    public double getActualEndExcel(){
        if(this.actualEnd!=null)
            return DateUtil.getExcelDate(this.actualEnd);
        else
            return 0;
    }
    public String getDate(){
        return this.date;
    }
    public long getDateLong(){
        SimpleDateFormat slimDateFormat = new SimpleDateFormat("MM/dd/yy",Locale.US);
        if(this.date!=null)
            try {
                return slimDateFormat.parse( this.date ).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        else
            return 0;
    }
    public double getDateExcel(){
        SimpleDateFormat slimDateFormat = new SimpleDateFormat("MM/dd/yy",Locale.US);
        if(this.date!=null){
            try {
                return DateUtil.getExcelDate(slimDateFormat.parse( this.date ));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            return 0;
        }
    }
    public String getElapsedPlannedTime(){
        return this.elapsedPlannedTime;
    }
    public String getElapsedActualTime(){
        return this.elapsedActualTime;
    }
    public String getTimeAhead(){
        return this.timeAhead;
    }
    public void calculateElapsedPlannedTime(){
        if(this.plannedStart!=null && this.plannedEnd!=null){
            long difference_in_time = this.plannedEnd.getTime() - this.plannedStart.getTime();
            long difference_in_hours = (difference_in_time/(1000*60*60))%24;
            long difference_in_minutes = (difference_in_time/(1000*60))%60;
            this.elapsedPlannedTime = difference_in_hours + "." + difference_in_minutes;
        }
    }
    public void calculateElapsedActualTime(){
        if(this.actualStart!=null && this.actualEnd!=null){
            long difference_in_time = this.actualEnd.getTime() - this.actualStart.getTime();
            long difference_in_hours = (difference_in_time/(1000*60*60))%24;
            long difference_in_minutes = (difference_in_time/(1000*60))%60;
            this.elapsedActualTime = difference_in_hours + "." + difference_in_minutes;
        }

    }
    public void calculateTimeAhead(){
        if(this.actualStart!=null && this.actualEnd!=null && this.plannedEnd!=null && this.plannedStart!=null){
            long plannedDifference = this.plannedEnd.getTime() - this.plannedStart.getTime();
            long actualDifference = this.actualEnd.getTime() - this.actualStart.getTime();
            if(actualDifference > plannedDifference){//driver is behind
                long difference_in_time = actualDifference - plannedDifference;
                long difference_in_hours = (difference_in_time/(1000*60*60))%24;
                long difference_in_minutes = (difference_in_time/(1000*60))%60;
                this.timeAhead = "-"+difference_in_hours+"."+difference_in_minutes;
            }else{
                long difference_in_time = plannedDifference - actualDifference;
                long difference_in_hours = (difference_in_time/(1000*60*60))%24;
                long difference_in_minutes = (difference_in_time/(1000*60))%60;
                this.timeAhead = "+"+difference_in_hours+"."+difference_in_minutes;
            }
        }
    }
    public void printDriver(){
        System.out.println("DRIVER: "+this.name+" DATE: "+this.date + " PLANNED START: " + this.plannedStart + " PLANNED END: "+this.plannedEnd);
    }
    public String printToCSV(){
        return this.date + ","
                + this.ID + ","
                + this.name + ","
                + this.routeID + ","
                + this.getPlannedStartExcel() + ","
                + this.getPlannedEndExcel() + ","
                + this.elapsedPlannedTime + ","
                + this.getActualStartExcel() + ","
                + this.getActualEndExcel() + ","
                + this.elapsedActualTime + ","
                + this.timeAhead + "\n";
    }
}

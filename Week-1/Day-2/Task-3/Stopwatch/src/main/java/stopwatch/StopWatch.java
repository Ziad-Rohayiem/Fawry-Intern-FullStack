package stopwatch;

public class StopWatch {
    private int minutes;
    private int hours;
    private int days;
    int hoursInDay;
    
    // default day
    public StopWatch() {
        this.hoursInDay = 24;
    }

    public StopWatch(int hoursInDay) {
        this.hoursInDay = hoursInDay;
    }

    public void record(int minutes) {
        if (minutes >= 0) {
            this.minutes += minutes;
            if (this.minutes >= 60) {
                this.hours += this.minutes / 60;
                this.minutes %= 60;
            }
            if(this.hours >= this.hoursInDay){
                this.days += this.hours/this.hoursInDay;
                this.hours %= this.hoursInDay;
            }
        }
    }

    public int getMinutes() {
        return minutes;
    }

     public int getHours() {
        return hours;
    }

    public int getDays() {
        return days;
    }
}

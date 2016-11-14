package co.evelabs.beep;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Patient {
    public String name;
    public String bed;
    public int volume,dropFactor,duration;


    public Patient() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setBed(String bed) {
        this.bed = bed;
    }
    public void setVolume (int volume) {this.volume = volume;}
    public void setDuration (int duration) {this.duration = duration;}
    public void setDropFactor (int dropFactor) {this.dropFactor = dropFactor;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("name", name );
        result.put("bed", bed);
        result.put("volume", volume);
        result.put("duration", duration);
        result.put("dropFactor", dropFactor);


        return result;
    }

    public String getName() {
        return name;
    }
    public String getBed() {
        return bed;
    }
    public int getVolume(){
        return volume;
    }
    public int getDuration(){
        return (duration);
    }

    public int getDropFactor(){
        return (dropFactor);
    }

    public String dpfStr(){return  Integer.toString(dropFactor) + " dpf"; };
    public String VolumeStr(){return  Integer.toString(volume) + " ml"; };
    public String DurationInFormat(){
        int hr=((duration/60));
        String min;
        if(hr<1) {
             min = "30";
        }
                    else {
             min = "00";

        }
        String Str="0"+Integer.toString(hr)+":"+min+ " hrs";
   return Str;
    }

}

package project.Model;

import java.io.Serializable;
import java.util.*;

public class VectorClock implements Serializable {

    private final Map<UUID, Integer> vectorClock;

    public VectorClock(){
        vectorClock = new HashMap<>();
    }

    public VectorClock(Map<UUID, Integer> vc){
        vectorClock = new HashMap<>(vc);
    }

    public Integer getValue(UUID key){
        return vectorClock.get(key);
    }

    public Collection<UUID> getKeys(){
        return vectorClock.keySet();
    }

    public Collection<Integer> getValues(){
        return vectorClock.values();
    }

    public Map<UUID, Integer> getMap(){
        return vectorClock;
    }

    public void add(UUID key, int value){
        vectorClock.put(key, value);
    }

    public void replace(UUID key, int value){
        vectorClock.replace(key, value);
    }

    public int sum() {
        int count = 0;
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            count+=entry.getValue();
        }
        return count;
    }

    public boolean equals(VectorClock other){
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!entry.getValue().equals(other.getValue(entry.getKey()))){
                return false;
            }
        }
        return true;
    }

    public boolean isLessThan(VectorClock other){
        if(equals(other)){
            return false;
        }
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!(entry.getValue() <= other.getValue(entry.getKey()))){
                return false; // an element is bigger than one of the other vc
            }
        }
        return true;
    }

    public boolean isLessThanOrEqual(VectorClock other){
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!(entry.getValue() <= other.getValue(entry.getKey()))){
                return false; // an element is bigger than one of the other vc
            }
        }
        return true;
    }

    public VectorClock copySlice(UUID uuidToRemove){
        VectorClock clone = new VectorClock();
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!entry.getKey().equals(uuidToRemove)){
                clone.add(entry.getKey(), entry.getValue());
            }
        }
        return clone;
    }

}

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

    public VectorClock copyWithout(UUID uuid){
        VectorClock vc = new VectorClock(vectorClock);
        vc.getMap().remove(uuid);
        return vc;
    }

    public boolean olderThan(VectorClock vc){
        for(Map.Entry<UUID, Integer> entry: vectorClock.entrySet()){
            if(entry.getValue() > vc.getValue(entry.getKey())){
                return false;
            }
        }
        return true;
    }

}

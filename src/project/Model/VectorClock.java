package project.Model;

import java.io.Serializable;
import java.util.*;


/**
 * This class represent the vector clock, useful for causally ordered delivery.
 * It is a Map that binds the UUID of a Peer and the event counter.
 */
public class VectorClock implements Serializable {

    private final Map<UUID, Integer> vectorClock;

    /**
     * Builds an instance of the vector clock from scratch as an HashMap.
     */
    public VectorClock(){
        vectorClock = new HashMap<>();
    }

    /**
     * Builds an instance of the vector clock, assigning the Hashmap passed by parameter.
     * @param vc The hashmap of the vector clock
     */
    public VectorClock(Map<UUID, Integer> vc){
        vectorClock = new HashMap<>(vc);
    }

    //GETTERS

    /**
     * Returns the value of the vector clock bound to the key.
     * 
     * @param key The key in the hashmap
     * @return The value bound to the key
     */
    public Integer getValue(UUID key){
        return vectorClock.get(key);
    }

    /**
     * Returns all the keys of the vector clock.
     * 
     * @return The collection of keys of the vector clock.
     */
    public Collection<UUID> getKeys(){
        return vectorClock.keySet();
    }

    /**
     * Returns all the values of the vector clock.
     * 
     * @return The collection of values of the vector clock.
     */
    public Collection<Integer> getValues(){
        return vectorClock.values();
    }

    /**
     * Returns the hashmap.
     * 
     * @return The hashmap.
     */
    public Map<UUID, Integer> getMap(){
        return vectorClock;
    }

    // PUBLIC METHODS

    /**
     * Adds a new pair key-value in the vector clock. If the key was already
     * in the vector clock, just substitutes its value.
     * 
     * @param key To be added to the vector clock.
     * @param value To be added to the vector clock.
     */
    public void add(UUID key, int value){
        vectorClock.put(key, value);
    }

    /**
     * Replaces the entry for the specified key, only if the key is found.
     * 
     * @param key the key
     * @param value the new value
     */
    public void replace(UUID key, int value){
        vectorClock.replace(key, value);
    }

    /**
     * Sums all the values of the vector clock.
     * 
     * @return The sum of all the values of the vector clock.
     */
    public int sum() {
        int count = 0;
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            count+=entry.getValue();
        }
        return count;
    }

    /**
     * Checks if all the entries of this vector clock's entries are equal
     * to those of the vector clock passed as a parameter.
     * 
     * @param other The vector clock to compare.
     * @return True if all the entries are equal, otherwise False.
     */
    public boolean equals(VectorClock other){
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!entry.getValue().equals(other.getValue(entry.getKey()))){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all the entries of this vector clock are less than or equal 
     * to those of the vector clock passed as a parameter. Moreover, the two
     * vector clocks can not have all the entries as the same.
     *  
     * @param other The vector clock to compare.
     * @return True if the condition holds, otherwise False.
     */
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

    /**
     * Checks if all the entries of this vector clock are less than or equal 
     * to those of the vector clock passed as a parameter.
     *  
     * @param other The vector clock to compare.
     * @return True if the condition holds, otherwise False.
     */
    public boolean isLessThanOrEqual(VectorClock other){
        for(Map.Entry<UUID, Integer> entry : vectorClock.entrySet()){
            if(!(entry.getValue() <= other.getValue(entry.getKey()))){
                return false; // an element is bigger than one of the other vc
            }
        }
        return true;
    }

    /**
     * Returns a copy of the vector clock without a couple key-value.
     * 
     * @param uuidToRemove The key of the couple to remove.
     * @return A copy of the vector clock without the specified couple key-value.
     */
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

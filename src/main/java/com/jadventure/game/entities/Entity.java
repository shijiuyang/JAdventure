package com.jadventure.game.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jadventure.game.GameBeans;
import com.jadventure.game.QueueProvider;
import com.jadventure.game.items.Item;
import com.jadventure.game.items.ItemStack;
import com.jadventure.game.items.Storage;
import com.jadventure.game.repository.ItemRepository;

/**
 * superclass for all entities (includes player, monsters...)
 */
public abstract class Entity {
    // @Resource
    protected ItemRepository itemRepo = GameBeans.getItemRepository();
    
    // All entities can attack, have health, have names
    private int healthMax;
    private int health;
    private String name;
    private String intro;
    private int level;
    // Statistics
    private int strength;
    private int intelligence;
    private int dexterity;
    private int luck;
    private int stealth;
    private int gold;
    private double damage = 30;
    private double magicDamage = 0;
    private double critChance = 0.0;
    private int armour;
    private String weapon = "hands";
    private Map<EquipmentLocation, Item> equipment;
    protected Storage storage;

    public Entity() {
    	this(100, 100, "default", 0, null, new HashMap<EquipmentLocation, Item>());
    }
    
    public Entity(int healthMax, int health, String name, int gold, Storage storage, Map<EquipmentLocation, Item> equipment) {
        this.healthMax = healthMax;
        this.health = health;
        this.name = name;
        this.gold = gold;
        if (storage != null) {
        	this.storage = storage;
        }
        else {
        	this.storage = new Storage(300);
        }
	    this.equipment = equipment;
    }
    

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int health) {
        if (health > healthMax) {
            health = healthMax;
        }
        this.health = health;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public int getArmour() {
        return armour;
    }

    public void setArmour(int armour) {
        this.armour = armour;
    }

    public int getHealthMax() {
        return healthMax;
    }

    public void setHealthMax(int healthMax) {
        this.healthMax = healthMax;
        if (health > healthMax) {
            health = healthMax;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
     
    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIntro() {
        return this.intro;
    }
    
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<EquipmentLocation, Item> getEquipment() {
        return Collections.unmodifiableMap(equipment);
    }

    public void setEquipment(Map<EquipmentLocation, Item> equipment) {
        this.equipment = equipment;
    }

    public int getStrength() {
        return strength;
    }
    
    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }
 
    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public int getStealth() {
        return stealth;
    }

    public void setStealth(int stealth) {
        this.stealth = stealth;
    }

    public String getWeapon() {
        return weapon;
    }

    public double getMagicDamage() {
        return magicDamage;
    }

    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    public Map<String, String> equipItem(EquipmentLocation place, Item item) {
        double oldDamage = this.damage;
        double oldMagicDamage = this.magicDamage;
        int oldArmour = this.armour;
        if (place == null) {
            place = item.getPosition();
        }
        if (equipment.get(place) != null) {
            unequipItem(equipment.get(place));
        }
        if (place == EquipmentLocation.BOTH_HANDS) {
            unequipTwoPlaces(EquipmentLocation.LEFT_HAND, EquipmentLocation.RIGHT_HAND);
        } else if (place == EquipmentLocation.BOTH_ARMS) {
            unequipTwoPlaces(EquipmentLocation.LEFT_ARM, EquipmentLocation.RIGHT_ARM);
        } 
        Item bothHands = equipment.get(EquipmentLocation.BOTH_HANDS);
        if (bothHands != null && (EquipmentLocation.LEFT_HAND == place || EquipmentLocation.RIGHT_HAND == place)) { 
            unequipItem(bothHands);
        }
        Item bothArms = equipment.get(EquipmentLocation.BOTH_ARMS);
        if (bothArms != null && (place == EquipmentLocation.LEFT_ARM || place == EquipmentLocation.RIGHT_ARM)) { 
            unequipItem(bothArms);
        }
        equipment.put(place, item);
        // Update weapon field for weapon type items
        if (item.getType().startsWith("weapon") || place == EquipmentLocation.BOTH_HANDS 
            || place == EquipmentLocation.LEFT_HAND || place == EquipmentLocation.RIGHT_HAND) {
            this.weapon = item.getId();
        }
        removeItemFromStorage(item);
        Map<String, Integer> stats = item.getProperties();
        Map<String, String> result = new HashMap<>();
        if (stats != null) {
            for (Map.Entry<String, Integer> stat : stats.entrySet()) {
                String key = stat.getKey();
                int value = stat.getValue();
                switch (key) {
                    case "damage": {
                        this.damage += value;
                        result.put("damage", String.valueOf(damage - oldDamage));
                        break;
                    }
                    case "magicDamage": {
                        this.magicDamage += value;
                        result.put("magicDamage", String.valueOf(magicDamage - oldMagicDamage));
                        break;
                    }
                    case "armour": {
                        this.armour += value;
                        result.put("armour", String.valueOf(armour - oldArmour));
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void unequipTwoPlaces(EquipmentLocation leftLocation, EquipmentLocation rightLocation) {
        Item left = equipment.get(leftLocation);
        Item right = equipment.get(rightLocation);
        if (left != null) {
            unequipItem(left);
        }
        if (right != null) { 
            unequipItem(right);
        }
    }

    public Map<String, String> unequipItem(Item item) {
        double oldDamage = this.damage;
        double oldMagicDamage = this.magicDamage;
        int oldArmour = this.armour;
        Map<String, Integer> stats = item.getProperties();
        Map<String, String> result = new HashMap<>();
        if (stats != null) {
            for (Map.Entry<String, Integer> stat : stats.entrySet()) {
                String key = stat.getKey();
                int value = stat.getValue();
                switch (key) {
                    case "damage": {
                        this.damage -= value;
                        result.put("damage", String.valueOf(damage - oldDamage));
                        break;
                    }
                    case "magicDamage": {
                        this.magicDamage -= value;
                        result.put("magicDamage", String.valueOf(magicDamage - oldMagicDamage));
                        break;
                    }
                    case "armour": {
                        this.armour -= value;
                        result.put("armour", String.valueOf(armour - oldArmour));
                        break;
                    }
                }
            }
        }
        EquipmentLocation place = item.getPosition();
        if (equipment.get(place) == item) {
            equipment.remove(place);
            // Reset weapon field if unequipping a weapon
            if (item.getType().startsWith("weapon") || place == EquipmentLocation.BOTH_HANDS 
                || place == EquipmentLocation.LEFT_HAND || place == EquipmentLocation.RIGHT_HAND) {
                this.weapon = "hands";
            }
        }
        if (!item.equals(itemRepo.getItem("hands"))) {
            addItemToStorage(item);
        }
        return result;
    }

    public void printEquipment() {
        QueueProvider.offer("\n------------------------------------------------------------");
        QueueProvider.offer("Equipped Items:");
        if (equipment.keySet().size() == 0) {
            QueueProvider.offer("--Empty--");
        } else {
            int i = 0;
            Item hands = itemRepo.getItem("hands");
            Map<EquipmentLocation, String> locations = new HashMap<>();
            locations.put(EquipmentLocation.HEAD, "Head");
            locations.put(EquipmentLocation.CHEST, "Chest");
            locations.put(EquipmentLocation.LEFT_ARM, "Left arm");
            locations.put(EquipmentLocation.LEFT_HAND, "Left hand");
            locations.put(EquipmentLocation.RIGHT_ARM, "Right arm");
            locations.put(EquipmentLocation.RIGHT_HAND, "Right hand");
            locations.put(EquipmentLocation.BOTH_HANDS, "Both hands");
            locations.put(EquipmentLocation.BOTH_ARMS, "Both arms");
            locations.put(EquipmentLocation.LEGS, "Legs");
            locations.put(EquipmentLocation.FEET, "Feet");
            for (Map.Entry<EquipmentLocation, Item> item : equipment.entrySet()) {
                if (item.getKey() != null && !hands.equals(item.getValue()) && item.getValue() != null) {
                    QueueProvider.offer(locations.get(item.getKey()) + " - " + item.getValue().getName());
                } else {
                    i++;
                }
            }
            if (i == equipment.keySet().size()) {
                QueueProvider.offer("--Empty--");
            }
        }
        QueueProvider.offer("------------------------------------------------------------"); 
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void printStorage() {
       storage.display();
    } 
    
    public void addItemToStorage(Item item) {
        storage.addItem(new ItemStack(1, item));
    }

    public void removeItemFromStorage(Item item) {
        storage.removeItem(new ItemStack(1, item)); 
    }

}

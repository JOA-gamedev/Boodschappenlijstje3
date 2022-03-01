package com.NovaLabs.boodschappenlijstje;

public class Item {
    private final String itemName;
    private final String itemNumber;
    private final int itemUnit;

    public Item(String name, String number, int unit){
        this.itemName = name;
        this.itemNumber = number;
        this.itemUnit = unit;
    }

    public String getItemName() {
        return this.itemName;
    }
    public String getItemNumber(){
        return this.itemNumber;
    }
    public int getItemUnit(){
        return this.itemUnit;
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package graphdb;

/**
 *
 * @author melahi
 */
public class Property {

    private String property = null;
    private String object = null;
    private String propertyTpe = null;
    private String propertyCategory = null;
    private String objectType = null;

    public Property(String propertyT, String objectT, String propertyTpeT, String propertyCategoryT,String objectTypeT) {
        this.property = propertyT;
        this.object = objectT;
        this.propertyTpe = propertyTpeT;
        this.objectType = propertyCategoryT;
        this.propertyCategory = objectTypeT;

    }
    
    public Property(String propertyT, String objectT) {
        this.property = propertyT;
        this.object = objectT;
    }

    public String getPropertyCategory() {
        return propertyCategory;
    }

    public String getProperty() {
        return this.replace(this.property);
    }

    public String getObject() {
        return object;
    }

    public String getPropertyTpe() {
        return propertyTpe;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return "Property{" + "property=" + property + ", object=" + object + ", propertyTpe=" + propertyTpe + ", objectType=" + objectType + '}';
    }

    private String replace(String propertyT) {
        propertyT=propertyT.replace(" ", "_");
        return propertyT;
    }

}

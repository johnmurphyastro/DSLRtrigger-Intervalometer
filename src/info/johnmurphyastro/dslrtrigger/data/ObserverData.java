/*
 * DSLR Trigger is designed to control a DSLR camera to providing accurate
 * start and end exposure times
 * Copyright (C) 2018 - 2019  John Murphy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package info.johnmurphyastro.dslrtrigger.data;

/**
 * Observer supplied data
 * @author John Murphy
 */
public class ObserverData {
    // Observer data
    private final String name;
    private final String email;
    private final String location;
    private final String camera;
    private final String lens;
    private final String comment;
    
    /**
     * @param name
     * @param email
     * @param location
     * @param camera
     * @param lens
     * @param comment 
     */
    public ObserverData(String name, String email, String location,
            String camera, String lens, String comment){
        this.name = name;
        this.email = email;
        this.location = location;
        this.camera = camera;
        this.lens = lens;
        this.comment = comment;
    }
    
    /**
     * @return Observer name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Observer email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return Observer latitude, longitude, altitude
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return Camera model
     */
    public String getCamera() {
        return camera;
    }

    /**
     * @return Camera lens / telescope focal length and focal ratio
     */
    public String getLens() {
        return lens;
    }

    /**
     * @return user supplied comment (eg BST)
     */
    public String getComment() {
        return comment;
    }
}

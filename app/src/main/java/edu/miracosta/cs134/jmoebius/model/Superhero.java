package edu.miracosta.cs134.jmoebius.model;

/**
 * Represents a Superhero for the purposes of the CS134Superheroes, including the Superhero's name,
 * superpower, one thing everyone should know, and the file name (including path) for their image.
 *
 * @author Jacob Moebius
 * @version 1.0
 */
public class Superhero {
    private String mName;
    private String mSuperpower;
    private String mOneThing;
    private String mFileName;

    /**
     * Instantiates a new <code>Superhero</code> given its name, superpower, one thing, and file
     * name.
     * @param name The name of the <code>Superhero</code>
     * @param superpower The superpower of the <code>Superhero</code>
     * @param superpower The one thing of the <code>Superhero</code>
     * @param superpower The file name of the <code>Superhero</code>
     */
    public Superhero(String name, String superpower, String oneThing, String fileName) {
        mName = name;
        mSuperpower = superpower;
        mOneThing = oneThing.replaceAll(" ", "_");
        mFileName = fileName;
    }

    /**
     * Gets the name of the <code>Superhero</code>.
     * @return The name of the <code>Superhero</code>
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the superpower of the <code>Superhero</code>.
     * @return The superpower of the <code>Superhero</code>
     */
    public String getSuperpower() {
        return mSuperpower;
    }

    /**
     * Gets the one thing of the <code>Superhero</code>.
     * @return The one thing of the <code>Superhero</code>
     */
    public String getOneThing() {
        return mOneThing;
    }

    /**
     * Gets the file name of the <code>Superhero</code> with its path. For example:
     * jmoebius.png
     * @return The file name of the <code>Superhero</code>
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * Compares two Superheroes for equality based on name, superpower, one thing, and file name.
     * @param o The other Superhero.
     * @return True if the Superheroes are the same, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Superhero superhero = (Superhero) o;
        if (!mName.equals(superhero.mName)) return false;
        if (!mSuperpower.equals(superhero.mSuperpower)) return false;
        if (!mOneThing.equals(superhero.mOneThing)) return false;
        return mFileName.equals(superhero.mFileName);
    }

    /**
     * Generates an integer based hash code to uniquely represent this <code>Superhero</code>.
     * @return An integer based hash code to represent this <code>Superhero</code>.
     */
    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mSuperpower.hashCode();
        result = 31 * result + mOneThing.hashCode();
        result = 31 * result + mFileName.hashCode();
        return result;
    }

    /**
     * Generates a text based representation of this <code>Superhero</code>.
     * @return A text based representation of this <code>Superhero</code>.
     */
    @Override
    public String toString() {
        return "Superhero{" +
                "Name='" + mName + '\'' +
                ", Superpower='" + mSuperpower + '\'' +
                ", One Thing='" + mOneThing + '\'' +
                ", FileName='" + mFileName + '\'' +
                '}';
    }
}

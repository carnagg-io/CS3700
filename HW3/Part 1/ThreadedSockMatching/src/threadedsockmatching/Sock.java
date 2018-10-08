/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 3-1
 * DUE         : 8 October 2018
 * DESCRIPTION : Defines the Sock class for use in production, matching, and
 *               washing.
 ******************************************************************************/

package threadedsockmatching;

public class Sock {
    
    private final Color color;
    
    Sock(Color color) {
        this.color = color;
    }
    
    public Color color() {
        return color;
    }
    
}

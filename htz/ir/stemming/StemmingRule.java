package htz.ir.stemming;

/**
 *
 * @author htaghizadeh
 */
class StemmingRule {
    
    public StemmingRule(String sBody, String sSubstitution, char sPoS, byte iMinLength, boolean bState) {
        this.setBody(sBody);
        this.setSubstitution(sSubstitution);
        this.setPoS(sPoS);
        this.setMinLength(iMinLength);        
        this.setState(bState);        
    }

    private String body;
    public final String getBody() {
        return body;
    }
    public final void setBody(String value) {
        body = value;
    }
    
    private String substitution;
    public final String getSubstitution() {
        return substitution;
    }
    public final void setSubstitution(String value) {
        substitution = value;
    }
    
    private char poS;
    public final char getPoS() {
        return poS;
    }
    public final void setPoS(char value) {
        poS = value;
    }
    
    private byte minLength;
    public final byte getMinLength() {
        return minLength;
    }
    public final void setMinLength(byte value) {
        minLength = value;
    }    
    
    private boolean state;
    public final boolean getState() {
        return state;
    }
    public final void setState(boolean value) {
        state = value;
    }       
}

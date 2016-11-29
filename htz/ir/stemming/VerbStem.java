package htz.ir.stemming;

/**
 *
 * @author htaghizadeh
 */
class VerbStem {
    
    public VerbStem(String sPast, String sPresent) {
        this.setPresent(sPresent);
        this.setPast(sPast);
    
    }
    private String present;
    public final String getPresent() {
        return present;
    }
    public final void setPresent(String value) {
        present = value;
    }
    
    private String past;
    public final String getPast() {
        return past;
    }
    public final void setPast(String value) {
        past = value;
    }    
}

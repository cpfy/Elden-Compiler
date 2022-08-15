package backend.Arm;

public class SpecialArm extends Arm{

    private String specialstr;

    // 例如push {...}
    public SpecialArm(String instrname, String specialstr){
        super(instrname);
        this.specialstr = specialstr;
    }

    @Override
    public String toString() {
        return super.getInstrname() + " " + specialstr;
    }

    public String getSpecialstr() {
        return specialstr;
    }
}

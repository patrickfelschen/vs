package beans;

/**
 * Java Bean zur Verwaltung von Formulatdaten. 
 * Zugriff über entsprechendes Servlet.
 * 
 * @author H.-J. Eikerling
 */
public class CalcBean {

    protected int op1, op2;

    public int getOp1() {
        return this.op1;
    }

    public int getOp2() {
        return this.op2;
    }

    public void setOp1(int v1) {
        this.op1 = v1;
    }

    public void setOp2(int v2) {
        this.op2 = v2;
    }

    public int getSumme() {
        System.err.print("getSumme() called");
        return op1 + op2;
    }
}

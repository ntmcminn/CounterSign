package org.alfresco.extension.countersign.signature;

/**
 *	Signature to Image: A supplemental script for Signature Pad that
 *	generates an image of the signature???s JSON output server-side using Java.
 *
 *	@project	signaturetoimage
 *	@author		Vinod Kiran (vinodkiran@usa.net)
 *	@link		http://github.com/vinodkiran/SignatureToImageJava
 *	@version	1.0.0
 */
public class SignatureLine {
    int lx, ly, mx, my;

    public SignatureLine() {
    }

    public int getLx() {
        return lx;
    }

    public void setLx(int lx) {
        this.lx = lx;
    }

    public int getLy() {
        return ly;
    }

    public void setLy(int ly) {
        this.ly = ly;
    }

    public int getMx() {
        return mx;
    }

    public void setMx(int mx) {
        this.mx = mx;
    }

    public int getMy() {
        return my;
    }

    public void setMy(int my) {
        this.my = my;
    }
}

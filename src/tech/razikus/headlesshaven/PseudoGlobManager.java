package tech.razikus.headlesshaven;

import haven.Astronomy;
import haven.PMessage;
import haven.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PseudoGlobManager {
    private AstronomyWrapper ast;
    public double gtime, sgtime, epoch = Utils.rtime();
    private static final double itimefac = 3.0;
    private double stimefac = itimefac, ctimefac = itimefac;
    public Color lightamb = null, lightdif = null, lightspc = null;
    public Color olightamb = null, olightdif = null, olightspc = null;
    public Color tlightamb = null, tlightdif = null, tlightspc = null;
    public double lightang = 0.0, lightelev = 0.0;
    public double olightang = 0.0, olightelev = 0.0;
    public double tlightang = 0.0, tlightelev = 0.0;
    public double lchange = -1;
    public double skyblend = 0.0;
    public ResourceInformationLazyProxy sky1 = null, sky2 = null;

    private Map<ResourceInformationLazyProxy, Object> wmap = new HashMap<ResourceInformationLazyProxy, Object>();

    private ResourceManager resourceManager;

    public PseudoGlobManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    private void updgtime(double sgtime, boolean inc) {
        double now = Utils.rtime();
        double delta = now - epoch;
        epoch = now;
        if ((this.sgtime == 0) || !inc || (Math.abs(sgtime - this.sgtime) > 500)) {
            this.gtime = this.sgtime = sgtime;
            return;
        }
        if ((sgtime - this.sgtime) > 1) {
            double utimefac = (sgtime - this.sgtime) / delta;
            double f = Math.min(delta * 0.01, 0.5);
            stimefac = (stimefac * (1 - f)) + (utimefac * f);
        }
        this.sgtime = sgtime;
    }

    public AstronomyWrapper getAst() {
        return ast;
    }

    public void handleGlobalObject(PMessage msg) {

        boolean inc = msg.uint8() != 0;
        while (!msg.eom()) {
            String t = msg.string().intern();
            Object[] objectList = msg.list();
            int n = 0;
            if (t.equals("tm")) {
                updgtime(Utils.dv(objectList[n++]), inc);
            } else if (t.equals("astro")) {
                double dt = Utils.dv(objectList[n++]);
                double mp = Utils.dv(objectList[n++]);
                double yt = Utils.dv(objectList[n++]);
                boolean night = Utils.bv(objectList[n++]);
                Color mc = (Color) objectList[n++];
                int is = (n < objectList.length) ? Utils.iv(objectList[n++]) : 1;
                double sp = (n < objectList.length) ? Utils.dv(objectList[n++]) : 0.5;
                double sd = (n < objectList.length) ? Utils.dv(objectList[n++]) : 0.5;
                double years = (n < objectList.length) ? Utils.dv(objectList[n++]) : 0.5;
                double ym = (n < objectList.length) ? Utils.dv(objectList[n++]) : 0.5;
                double md = (n < objectList.length) ? Utils.dv(objectList[n++]) : 0.5;
                ast = new AstronomyWrapper(dt, mp, yt, night, mc, is, sp, sd, years, ym, md);
            } else if (t.equals("light")) {
                synchronized (this) {
                    tlightamb = (Color) objectList[n++];
                    tlightdif = (Color) objectList[n++];
                    tlightspc = (Color) objectList[n++];
                    tlightang = Utils.dv(objectList[n++]);
                    tlightelev = Utils.dv(objectList[n++]);
                    if (inc) {
                        olightamb = lightamb;
                        olightdif = lightdif;
                        olightspc = lightspc;
                        olightang = lightang;
                        olightelev = lightelev;
                        lchange = 0;
                    } else {
                        lightamb = tlightamb;
                        lightdif = tlightdif;
                        lightspc = tlightspc;
                        lightang = tlightang;
                        lightelev = tlightelev;
                        lchange = -1;
                    }
                }
            } else if (t.equals("sky")) {
                synchronized (this) {
                    if (objectList.length < 1) {
                        sky1 = sky2 = null;
                        skyblend = 0.0;
                    } else {
                        Integer parsed = (Integer) objectList[n++];
                        // @TODO this may be wrong
                        sky1 = new ResourceInformationLazyProxy(this.resourceManager, parsed, null);
                        if (objectList.length < 2) {
                            sky2 = null;
                            skyblend = 0.0;
                        } else {
                            Integer parsedSky2 = (Integer) objectList[n++];
                            // @TODO this may be wrong
                            sky2 = new ResourceInformationLazyProxy(this.resourceManager, parsedSky2, null);
                            skyblend = Utils.dv(objectList[n++]);
                        }
                    }
                }
            } else if (t.equals("wth")) {
                System.out.println("Weather not supported yet - only object list parser");
                if (!inc) {
                    wmap.clear();
                }
                while (n < objectList.length) {
                    Integer parsed = (Integer) objectList[n++];
                    ResourceInformationLazyProxy resourceInformationLazyProxy = new ResourceInformationLazyProxy(this.resourceManager, parsed, null);
                    // @TODO this may be wrong

                    Object[] args = (Object[]) objectList[n++];
                    wmap.put(resourceInformationLazyProxy, args);
                }
            } else {
                System.err.println("Unknown globlob type: " + t);
            }
        }
    }

    @Override
    public String toString() {
        return "PseudoGlobManager{" +
                "ast=" + ast +
                ", gtime=" + gtime +
                ", sgtime=" + sgtime +
                ", epoch=" + epoch +
                ", stimefac=" + stimefac +
                ", ctimefac=" + ctimefac +
                ", lightamb=" + lightamb +
                ", lightdif=" + lightdif +
                ", lightspc=" + lightspc +
                ", olightamb=" + olightamb +
                ", olightdif=" + olightdif +
                ", olightspc=" + olightspc +
                ", tlightamb=" + tlightamb +
                ", tlightdif=" + tlightdif +
                ", tlightspc=" + tlightspc +
                ", lightang=" + lightang +
                ", lightelev=" + lightelev +
                ", olightang=" + olightang +
                ", olightelev=" + olightelev +
                ", tlightang=" + tlightang +
                ", tlightelev=" + tlightelev +
                ", lchange=" + lchange +
                ", skyblend=" + skyblend +
                ", sky1=" + sky1 +
                ", sky2=" + sky2 +
                ", resourceManager=" + resourceManager +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PseudoGlobManager that = (PseudoGlobManager) o;
        return Double.compare(gtime, that.gtime) == 0 && Double.compare(sgtime, that.sgtime) == 0 && Double.compare(epoch, that.epoch) == 0 && Double.compare(stimefac, that.stimefac) == 0 && Double.compare(ctimefac, that.ctimefac) == 0 && Double.compare(lightang, that.lightang) == 0 && Double.compare(lightelev, that.lightelev) == 0 && Double.compare(olightang, that.olightang) == 0 && Double.compare(olightelev, that.olightelev) == 0 && Double.compare(tlightang, that.tlightang) == 0 && Double.compare(tlightelev, that.tlightelev) == 0 && Double.compare(lchange, that.lchange) == 0 && Double.compare(skyblend, that.skyblend) == 0 && Objects.equals(ast, that.ast) && Objects.equals(lightamb, that.lightamb) && Objects.equals(lightdif, that.lightdif) && Objects.equals(lightspc, that.lightspc) && Objects.equals(olightamb, that.olightamb) && Objects.equals(olightdif, that.olightdif) && Objects.equals(olightspc, that.olightspc) && Objects.equals(tlightamb, that.tlightamb) && Objects.equals(tlightdif, that.tlightdif) && Objects.equals(tlightspc, that.tlightspc) && Objects.equals(sky1, that.sky1) && Objects.equals(sky2, that.sky2) && Objects.equals(resourceManager, that.resourceManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ast, gtime, sgtime, epoch, stimefac, ctimefac, lightamb, lightdif, lightspc, olightamb, olightdif, olightspc, tlightamb, tlightdif, tlightspc, lightang, lightelev, olightang, olightelev, tlightang, tlightelev, lchange, skyblend, sky1, sky2, resourceManager);
    }
}

package itdelatrisu.opsu;

import org.newdawn.slick.Graphics;

import java.time.LocalDateTime;

import org.newdawn.slick.Color;

import itdelatrisu.opsu.beatmap.HitObject;
import itdelatrisu.opsu.objects.GameObject;
import itdelatrisu.opsu.objects.curves.Bezier2;
import itdelatrisu.opsu.objects.curves.Vec2f;

public class AutodanceMod {
    public float controlPointsVectorsScale;
    public int circleSize;
    public int trackPosition;
    public Vec2f center;

    public int autoDanceModObjectIndex;
    public HitObject[] hitObjects;
    public GameObject[] gameObjects;

    public Graphics g;

    public AutodanceMod() {
        autoDanceModObjectIndex = 0;
    }

    public void Update() {
        CalculateObjectIndex();
    }

    private void CalculateObjectIndex(){
        while(autoDanceModObjectIndex < gameObjects.length) {
            if( trackPosition >= gameObjects[autoDanceModObjectIndex].getEndTime()) {
                autoDanceModObjectIndex++;
            }else{
                return;
            }
		}
    }

    public Vec2f ComputePosition(boolean debug) {
        Vec2f result = new Vec2f();
        int startTime, endTime;

        Vec2f pstart;
        Vec2f cp1 = new Vec2f();
        Vec2f cp2 = new Vec2f();
        Vec2f pend;

        // System.out.println("test");

        // if(autoDanceModObjectIndex > 0 && trackPosition < gameObjects[autoDanceModObjectIndex-1].getEndTime()) {
        //     System.out.println("popsute");
        // }
        

        if(autoDanceModObjectIndex < hitObjects.length) {
            endTime = hitObjects[autoDanceModObjectIndex].getTime();
            pend = gameObjects[autoDanceModObjectIndex].getPointAt(endTime);
            
            
            //cp1
            if(autoDanceModObjectIndex == 0) {
                //autoDanceModObjectIndex-1 doesnt exist yet
                startTime = 0;
                pstart = center;
                cp1 = center;
            } else if(hitObjects[autoDanceModObjectIndex-1].isSlider()) {
                // previous object is slider
                startTime = gameObjects[autoDanceModObjectIndex-1].getEndTime();
                pstart = gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime);
                float angle = hitObjects[autoDanceModObjectIndex-1].getSliderCurve(true).getEndAngle()*(float)Math.PI/180.0f;
                Vec2f a = new Vec2f(1,0).rotate(angle);
                //Vec2f a = gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime-50).cpy().sub(gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime));
                //float len = a.len();
                a.rotate(Math.PI).scale(controlPointsVectorsScale*500).add(gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime));
                cp1 = a;
            } else {
                // previous objects is circle or spinner
                startTime = gameObjects[autoDanceModObjectIndex-1].getEndTime();
                pstart = gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime);
                Vec2f a = gameObjects[autoDanceModObjectIndex].getPointAt(endTime).cpy().sub(gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime));
                float len = a.len();
                a.normalize().rotate(Math.PI/2).scale(controlPointsVectorsScale*len).add(gameObjects[autoDanceModObjectIndex-1].getPointAt(startTime));
                cp1 = a;
            }

            //cp2
            if(hitObjects[autoDanceModObjectIndex].isCircle()) {
                if(autoDanceModObjectIndex + 1 < hitObjects.length) {
                    Vec2f b = gameObjects[autoDanceModObjectIndex+1].getPointAt(endTime).cpy().sub(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                    float len = b.len();
                    if(len > 0) {
                        b.normalize().rotate(3*Math.PI/2).scale(controlPointsVectorsScale*len).add(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                    } else {
                        b = cp1.cpy().normalize().scale(circleSize*3).add(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                    }
                    cp2 = b;
                } else {
                    Vec2f b = center.cpy().sub(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                    float len = b.len();
                    b.normalize().rotate(3*Math.PI/2).scale(controlPointsVectorsScale*len).add(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                    cp2 = b;
                }
            } else {
                float angle = hitObjects[autoDanceModObjectIndex].getSliderCurve(true).getStartAngle()*(float)Math.PI/180.0f;
                Vec2f b = new Vec2f(1,0).rotate(angle);
                b.rotate(Math.PI).scale(controlPointsVectorsScale*500).add(gameObjects[autoDanceModObjectIndex].getPointAt(endTime));
                cp2 = b;
            }

            Vec2f abc = pend.cpy().sub(pstart);
            
            if(abc.len() < circleSize) {
                cp1 = abc.cpy().scale(0.33f).add(pstart);
                cp2 = abc.cpy().scale(0.66f).add(pstart);
            }
            Vec2f[] controlPoints = {pstart, cp1, cp2,pend};
            Bezier2 actualPath = new Bezier2(controlPoints);
            float progressInCurve = (float)(trackPosition - startTime) / (float)(endTime - startTime);
            result = actualPath.pointAt(progressInCurve);

            if(debug) {
                g.setColor(Color.green);
                float tmp = center.x/2/70;
                g.fillOval(cp1.x-tmp/2, cp1.y-tmp/2, tmp,tmp);
                g.fillOval(cp2.x-tmp/2, cp2.y-tmp/2, tmp,tmp);

                g.drawLine(pstart.x-tmp/2, pstart.y-tmp/2, cp1.x-tmp/2, cp1.y-tmp/2);
                g.drawLine(cp1.x-tmp/2, cp1.y-tmp/2, cp2.x-tmp/2, cp2.y-tmp/2);
                g.drawLine(cp2.x-tmp/2, cp2.y-tmp/2, pend.x-tmp/2, pend.y-tmp/2);
            }

        }
        return result;
    }

    
}
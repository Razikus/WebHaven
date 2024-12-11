package tech.razikus.headlesshaven;

import java.util.Objects;

public class CompositePoseState {
    private int poseSequence;
    private PoseData mainPoses;
    private PoseData transitionPoses;

    public CompositePoseState() {
        this.poseSequence = 0;
    }

    public void setPoseSequence(int poseSequence) {
        this.poseSequence = poseSequence;
    }

    public void setMainPoses(PoseData mainPoses) {
        this.mainPoses = mainPoses;
    }

    public void setTransitionPoses(PoseData transitionPoses) {
        this.transitionPoses = transitionPoses;
    }

    public int getPoseSequence() {
        return poseSequence;
    }

    public PoseData getMainPoses() {
        return mainPoses;
    }

    public PoseData getTransitionPoses() {
        return transitionPoses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositePoseState that = (CompositePoseState) o;
        return poseSequence == that.poseSequence && Objects.equals(mainPoses, that.mainPoses) && Objects.equals(transitionPoses, that.transitionPoses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poseSequence, mainPoses, transitionPoses);
    }

    @Override
    public String toString() {
        return "CompositePoseState{" +
                "poseSequence=" + poseSequence +
                ", mainPoses=" + mainPoses +
                ", transitionPoses=" + transitionPoses +
                '}';
    }
}
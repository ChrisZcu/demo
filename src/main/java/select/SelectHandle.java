package select;

import model.Trajectory;
import org.apache.commons.lang3.StringUtils;
import util.REGION;
import util.SharedObject;

import java.util.List;
import java.util.concurrent.Callable;

import static util.RegionAlg.*;


public class SelectHandle implements Callable {
    private REGION inter;
    private List<Trajectory> traj_list;
    private List<Integer> res;

    public SelectHandle(REGION inter, List<Trajectory> traj_list) {
        this.inter = inter;
        this.traj_list = traj_list;
    }
/*
    @Override
    public void run() {
        SharedObject instance = SharedObject.getInstance();
        switch (inter) {
            case ALLIN:
                res = getAllIn(traj_list, instance.getRegion_a());
                break;
            case O_D:
                res = getODTraj(traj_list, instance.getRegion_o(), instance.getRegion_d());
                break;
            case O_D_W:
                res = getODWTraj(traj_list, instance.getRegion_o(), instance.getRegion_d(), instance.getRegion_w());
                break;
            case WAY_POINT:
                res = getWayPointTraj(traj_list, instance.getRegion_w());
                break;
        }
        System.out.println("into thread: res number = " + res.size());
    }
*/
    @Override
    public Object call() throws Exception {
        SharedObject instance = SharedObject.getInstance();
        switch (inter) {
            case ALLIN:
                res = getAllIn(traj_list, instance.getRegionA());
                break;
            case O_D:
                res = getODTraj(traj_list, instance.getRegionO(), instance.getRegionD());
                break;
            case O_D_W:
                res = getODWTraj(traj_list, instance.getRegionO(), instance.getRegionD(), instance.getRegionW());
                break;
            case WAY_POINT:
                res = getWayPointTraj(traj_list, instance.getRegionW());
                break;
        }
        System.out.println("into thread: res number = " + res.size());
        return StringUtils.join(res,",");
    }
}

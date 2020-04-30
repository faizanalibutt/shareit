package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object;


import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.database.AccessDatabase;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.AssigneeNotFoundException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.ConnectionNotFoundException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.DeviceNotFoundException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.exception.TransferGroupNotFoundException;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup;
import com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.util.NetworkDeviceLoader;

/**
 * created by: Veli
 * date: 9.01.2018 18:40
 */

public class TransferInstance {
    private com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice mDevice;
    private com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup mGroup;
    private com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection mConnection;
    private com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup.Assignee mAssignee;

    // false means "to find connection first"
    public TransferInstance(AccessDatabase database, long groupId, String using, boolean findDevice) throws TransferGroupNotFoundException, DeviceNotFoundException, ConnectionNotFoundException, AssigneeNotFoundException {
        buildAll(database, groupId, using, findDevice);
    }

    private TransferInstance() {

    }

    protected void buildAll(AccessDatabase database, long groupId, String using, boolean findDevice) throws TransferGroupNotFoundException, DeviceNotFoundException, ConnectionNotFoundException, AssigneeNotFoundException {
        buildGroup(database, groupId);

        if (findDevice) {
            buildDevice(database, using);
            buildAssignee(database, mGroup, mDevice);
            buildConnection(database, mAssignee);
        } else {
            buildConnection(database, using);
            buildDevice(database, mConnection.deviceId);
            buildAssignee(database, mGroup, mDevice);
        }

        NetworkDeviceLoader.processConnection(database, getDevice(), getConnection());

        if (!getAssignee().connectionAdapter.equals(getConnection().adapterName)) {
            getAssignee().connectionAdapter = getConnection().adapterName;
            database.publish(getAssignee());
        }
    }

    protected void buildAssignee(AccessDatabase database, com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup group, com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice device) throws AssigneeNotFoundException {
        if (mAssignee != null)
            return;

        try {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup.Assignee assignee = new com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup.Assignee(group, device);

            database.reconstruct(assignee);

            mAssignee = assignee;
        } catch (Exception e) {
            throw new AssigneeNotFoundException();
        }
    }

    protected void buildConnection(AccessDatabase database, String connectionAddress) throws ConnectionNotFoundException {
        if (mConnection != null)
            return;

        try {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection connection = new com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection(connectionAddress);

            database.reconstruct(connection);

            mConnection = connection;
        } catch (Exception e) {
            throw new ConnectionNotFoundException();
        }
    }

    protected void buildConnection(AccessDatabase database, com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup.Assignee assignee) throws ConnectionNotFoundException {
        if (mConnection != null)
            return;

        try {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection connection = new com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection(assignee);

            database.reconstruct(connection);

            mConnection = connection;
        } catch (Exception e) {
            throw new ConnectionNotFoundException();
        }
    }

    protected void buildDevice(AccessDatabase database, String deviceId) throws DeviceNotFoundException {
        if (mDevice != null)
            return;

        try {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice device = new com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice(deviceId);

            database.reconstruct(device);

            mDevice = device;
        } catch (Exception e) {
            throw new DeviceNotFoundException();
        }
    }

    protected void buildGroup(AccessDatabase database, long groupId) throws TransferGroupNotFoundException {
        if (mGroup != null)
            return;

        try {
            com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup group = new com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup(groupId);

            database.reconstruct(group);

            mGroup = group;
        } catch (Exception e) {
            throw new TransferGroupNotFoundException();
        }
    }


    public com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup.Assignee getAssignee() {
        return mAssignee;
    }

    public com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice.Connection getConnection() {
        return mConnection;
    }

    public com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice getDevice() {
        return mDevice;
    }

    public com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup getGroup() {
        return mGroup;
    }

    public static class Builder {
        private TransferInstance mTransferInstance = new TransferInstance();

        public TransferInstance build(AccessDatabase database, long groupId, String using, boolean findDevice) throws AssigneeNotFoundException, DeviceNotFoundException, TransferGroupNotFoundException, ConnectionNotFoundException {
            mTransferInstance.buildAll(database, groupId, using, findDevice);
            return mTransferInstance;
        }

        public Builder supply(com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.TransferGroup group) {
            mTransferInstance.mGroup = group;
            return this;
        }

        public Builder supply(com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.object.NetworkDevice device) {
            mTransferInstance.mDevice = device;
            return this;
        }

        public Builder supply(NetworkDevice.Connection connection) {
            mTransferInstance.mConnection = connection;
            return this;
        }

        public Builder supply(TransferGroup.Assignee assignee) {
            mTransferInstance.mAssignee = assignee;
            return this;
        }
    }
}

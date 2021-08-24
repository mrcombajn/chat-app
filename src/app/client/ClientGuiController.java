package app.client;

public class ClientGuiController extends Client {
    private ClientGuiView view = new ClientGuiView(this);
    private ClientGuiModel model = new ClientGuiModel();


    public class GuiSocketThread extends Client.SocketThread {
        public void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }

        public void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }

        public void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }

        public void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

    public static void main(String[] args) {
        ClientGuiController controller = new ClientGuiController();
        controller.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    public void run() {
        getSocketThread().run();
    }

    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    public ClientGuiModel getModel() {
        return this.model;
    }
}
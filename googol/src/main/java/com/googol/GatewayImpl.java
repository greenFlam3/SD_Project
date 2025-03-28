import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

public class GatewayImpl extends UnicastRemoteObject implements GatewayService {

    // Para simplificar, utilizamos un único Storage Barrel.
    private SearchService storageBarrel;

    protected GatewayImpl(SearchService storageBarrel) throws RemoteException {
        super();
        this.storageBarrel = storageBarrel;
    }

    @Override
    public void indexPage(String url, String title, String text) throws RemoteException {
        // Delegar al Storage Barrel
        storageBarrel.indexPage(url, title, text);
    }

    @Override
    public Set<String> search(String query) throws RemoteException {
        return storageBarrel.search(query);
    }
}

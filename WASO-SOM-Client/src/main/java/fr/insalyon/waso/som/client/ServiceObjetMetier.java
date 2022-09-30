package fr.insalyon.waso.som.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author WASO Team
 */
public class ServiceObjetMetier {

    protected DBConnection dBConnection;
    protected JsonObject container;

    public ServiceObjetMetier(DBConnection dBConnection, JsonObject container) {
        this.dBConnection = dBConnection;
        this.container = container;
    }
    
    public void release() {
        this.dBConnection.close();
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();

            List<Object[]> listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT ORDER BY ClientID");

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","getListeClient", ex);
        }
    }

    public void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();
            List<Object[]> listeClients;
            denomination = denomination.toUpperCase();
            denomination = StringUtils.stripAccents(denomination);

            if (ville == null) {
                listeClients = this.dBConnection.launchQuery(
                        "SELECT ClientID, TypeClient, Denomination, Adresse, Ville"
                                + " FROM CLIENT c WHERE UPPER(c.Denomination) like '%' || ? || '%' "
                                + " ORDER BY ClientID", denomination);
            } else {
                ville = ville.toUpperCase();
                listeClients = this.dBConnection.launchQuery(
                        "SELECT ClientID, TypeClient, Denomination, Adresse, Ville"
                                + " FROM CLIENT c WHERE UPPER(c.Denomination) like '%' || ? || '%' "
                                + " AND UPPER(c.Ville) like '%' || ? || '%' "
                                + " ORDER BY ClientID", denomination, ville);
            }

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);



        } catch (DBException ex)
        {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","rechercherClientParDenomination", ex);
        }
    }

    public void rechercherClientParPersonne(int[] personneIds, String ville) throws ServiceException {
        try {
            if (personneIds.length == 0) return;
            // List of personneID as a String
            StringBuilder listComposer = new StringBuilder("(");
            for (int i = 0; i < personneIds.length - 1; i++) {
                listComposer.append(personneIds[i]).append(", ");
            }
            listComposer.append(personneIds[personneIds.length - 1]).append(")");
            // Query of ClientIDs for PersonneIDs
            List<Object[]> listePersonneClient = this.dBConnection.launchQuery(
                    "SELECT DISTINCT ClientID FROM COMPOSER WHERE PersonneID IN " +
                            listComposer);
            int[] clientIDs = listePersonneClient.stream().mapToInt(personne -> (int) personne[0]).toArray();
            // List of clients as String
            String listClientsStr = Arrays.toString(clientIDs).replaceAll("\\[", "(");
            listClientsStr = listClientsStr.replaceAll("]", ")");

            List<Object[]> listeClients;
            JsonArray jsonListe = new JsonArray();
            if (ville == null) {
                listeClients = this.dBConnection.launchQuery(
                        "SELECT ClientID, TypeClient, Denomination, Adresse, Ville"
                                + " FROM CLIENT c WHERE c.ClientID IN " + listClientsStr
                                + " ORDER BY ClientID");
            } else {
                ville = ville.toUpperCase();
                ville = StringUtils.stripAccents(ville);
                listeClients = this.dBConnection.launchQuery(
                        "SELECT ClientID, TypeClient, Denomination, Adresse, Ville"
                                + " FROM CLIENT c WHERE c.ClientID IN " + listClientsStr
                                + " AND UPPER(c.Ville) like '%' || ? || '%' "
                                + " ORDER BY ClientID", ville);
            }


            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);
        } catch (DBException ex)
        {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","rechercherClientParPersonne", ex);
        }
    }

}

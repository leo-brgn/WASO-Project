package fr.insalyon.waso.som.personne;

import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author WASO Team
 */
//@WebServlet(name = "ServiceObjetMetierServlet", urlPatterns = {"/ServiceObjetMetier"})
public class ServiceObjetMetierServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(JsonServletHelper.ENCODING_UTF8);

        try {

            String som = null;

            // cf. https://codebox.net/pages/java-servlet-url-parts
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                som = pathInfo.substring(1);
            }

            String somParameter = request.getParameter("SOM");
            if (somParameter != null) {
                som = somParameter;
            }

            DBConnection connection = new DBConnection(
                    this.getInitParameter("JDBC-Personne-URL"),
                    this.getInitParameter("JDBC-Personne-User"),
                    this.getInitParameter("JDBC-Personne-Password"),
                    "PERSONNE"
            );

            JsonObject container = new JsonObject();

            ServiceObjetMetier service = new ServiceObjetMetier(connection, container);

            boolean serviceCalled = true;

            if ("getListePersonne".equals(som)) {
                service.getListePersonne();
            } else if ("getPersonneParId".equals(som)) {
                String idPersonneParametre = request.getParameter("id-personne");
                if (idPersonneParametre == null) {
                    throw new ServiceException("Paramètres incomplets");
                }
                Integer idPersonne = Integer.parseInt(idPersonneParametre);
                // service.getPersonneParId(idPersonne);
            } else if ("rechercherPersonneParNom".equals(som)) {
                String nomPersonne = request.getParameter("nomPersonne");
                if (nomPersonne == null) {
                    throw new ServiceException("Paramètres incomplets");
                }
                 service.rechercherPersonneParNom(nomPersonne);
            } else if ("getPersonnesParIds".equals(som)) {
                String idsPersonnesParametre = request.getParameter("ids-personnes");
                if (idsPersonnesParametre == null) {
                    throw new ServiceException("Paramètres incomplets");
                }
                int[] idsPersonnes = Arrays.stream(idsPersonnesParametre.split(","))
                        .mapToInt(Integer::parseInt).toArray();
                service.getPersonnesParIds(idsPersonnes);
            } else {
                serviceCalled = false;
            }

            if (serviceCalled) {
                JsonServletHelper.printJsonOutput(response, container);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service SOM '" + som + "' not found");
            }

            service.release();

        } catch (DBException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB Exception: " + ex.getMessage());
            this.getServletContext().log("DB Exception in " + this.getClass().getName(), ex);
        } catch (ServiceException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service Exception: " + ex.getMessage());
            this.getServletContext().log("Service Exception in " + this.getClass().getName(), ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Service Objet Metier Servlet";
    }

}

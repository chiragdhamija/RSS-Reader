package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.BugReportDao;
import com.sismics.reader.core.dao.jpa.criteria.BugReportCriteria;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.model.jpa.BugReport;
import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Bug report REST resources.
 * 
 * @author adityamishra
 */
@Path("/bugreport")
public class BugReportResource extends BaseResource {
    /**
     * Creates a new bug report.
     * 
     * @param description Bug report description
     * @return Response
     * @throws JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBugReport(
            @FormParam("description") String description) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input
        if (description == null || description.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Description must not be empty");
        }
        
        // Create the bug report
        BugReportDao bugReportDao = new BugReportDao();
        BugReport bugReport = new BugReport();
        bugReport.setDescription(description);
        bugReport.setUsername(principal.getName());
        bugReport.setUserid(principal.getId());
        bugReport.setCreationDate(new Date());
        bugReport.setResolved(false);
    
        // Save to database
        bugReportDao.create(bugReport);
        
        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns all bug reports.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBugReports() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the current user id
        String userid = null;
        boolean isAdmin = hasBaseFunction(BaseFunction.ADMIN);
        if (!isAdmin) {
            userid = principal.getId();
        }
        
        // Get bug reports with filtering
        BugReportDao bugReportDao = new BugReportDao();
        BugReportCriteria criteria = new BugReportCriteria();
        
        // Set username filter for non-admin users
        if (userid != null) {
            criteria.setUserid(userid);
        }
        List<BugReportDto> bugReportDtoList = bugReportDao.findByCriteria(criteria);
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        JSONArray items = new JSONArray();
        for (BugReportDto bugReportDto : bugReportDtoList) {
            JSONObject item = new JSONObject();
            item.put("id", bugReportDto.getId());
            item.put("description", bugReportDto.getDescription());
            item.put("create_date", bugReportDto.getCreationDate().getTime());
            item.put("username", bugReportDto.getUsername());
            item.put("resolved", bugReportDto.isResolved());
            items.put(item);
        }
        response.put("bugs", items);
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a bug report by ID.
     *
     * @param id Bug report ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("/{id}/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBugReport(@PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        // Find the bug report
        BugReportDao bugReportDao = new BugReportDao();
        BugReport bugReport = bugReportDao.getById(id);

        if (bugReport == null) {
            throw new ClientException("NotFoundError", "Bug report not found");
        }
        // Delete the bug report
        bugReportDao.delete(id);

        // Build success response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("message", "Bug report deleted successfully");

        return Response.ok().entity(response).build();
    }

    /**
     * Resolves a bug report by ID.
     * 
     * @param id Bug report ID
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Path("/{id}/resolve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resolveBugReport(@PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        // Find the bug report
        BugReportDao bugReportDao = new BugReportDao();
        BugReport bugReport = bugReportDao.getById(id);

        if (bugReport == null) {
            throw new ClientException("NotFoundError", "Bug report not found");
        }
        // Delete the bug report
        bugReportDao.resolve(id);

        // Build success response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
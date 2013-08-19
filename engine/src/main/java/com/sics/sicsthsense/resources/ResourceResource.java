package com.sics.sicsthsense.resources;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import com.yammer.dropwizard.auth.Auth;

import com.sics.sicsthsense.core.*;
import com.sics.sicsthsense.jdbi.*;
import com.sics.sicsthsense.auth.*;
import com.sics.sicsthsense.auth.annotation.RestrictedTo;
import com.sics.sicsthsense.model.security.Authority;

@Path("/users/{userId}/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourceResource {
	private final StorageDAO storage;
	private final AtomicLong counter;
	private final Logger logger = LoggerFactory.getLogger(ResourceResource.class);
	private PollSystem pollSystem;

	public ResourceResource(StorageDAO storage, PollSystem pollSystem) {
		this.storage = storage;
		this.pollSystem = pollSystem;
		this.counter = new AtomicLong();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Timed
	public List<Resource> getResources(@RestrictedTo(Authority.ROLE_PUBLIC) User visitor, @PathParam("userId") long userId) {
		logger.info("Getting all user "+userId+" resources for visitor "+visitor.toString());

		List<Resource> resources = storage.findResourcesByOwnerId(userId);
		return resources;
	}

	@GET
	@Path("/{resourceId}")
	@Produces({MediaType.APPLICATION_JSON})
	@Timed
	public Resource getResource(@RestrictedTo(Authority.ROLE_PUBLIC) User visitor, @PathParam("userId") long userId, @PathParam("resourceId") long resourceId) {
		logger.info("Getting user/resource: "+userId+"/"+resourceId+" for user "+visitor.getId());
		Resource resource = storage.findResourceById(resourceId);
		if (resource == null) {
			logger.error("Resource "+resourceId+" does not exist!");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (resource.getOwner_id() != userId) {
			logger.error("User "+userId+" does not own resource "+resourceId);
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!resource.isReadable(visitor)) {
			logger.warn("Resource "+resource.getId()+" is not readable to user "+visitor.getId());
//			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return resource;
	}

	// post new resource definition 
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Timed
	public void postResource(@RestrictedTo(Authority.ROLE_USER) User visitor, @PathParam("userId") long userId, Resource resource) {
		logger.info("Adding user/resource:"+resource.getLabel());
		if (visitor.getId() != userId) {
			logger.error("Not allowed to add resource");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		insertResource(resource);
	}

	// put updated resource definition 
	@PUT
	@Path("/{resourceId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Timed
	public void updateResource(@RestrictedTo(Authority.ROLE_USER) User visitor, @PathParam("userId") long userId, @PathParam("resourceId") long resourceId, Resource resource) {
		logger.info("Updating resourceId:"+resourceId);
		if (visitor.getId() != userId) { // only owners
			logger.error("Not allowed to modify resource: "+resourceId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		updateResource(resourceId, resource);
	}

	@DELETE
	@Path("/{resourceId}")
	@Timed
	public void deleteResource(@RestrictedTo(Authority.ROLE_USER) User visitor, @PathParam("userId") long userId, @PathParam("resourceId") long resourceId) {
		logger.warn("Deleting resourceId:"+resourceId);
		Resource resource = storage.findResourceById(resourceId);
		if (resource==null) {
			logger.error("No resource to delete: "+resourceId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		if (visitor.getId() != userId) { // only owners
			logger.error("Not allowed to delete resource: "+resourceId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		storage.deleteResource(resourceId);
	}

	// Post data to the resource, and run data through its parsers
	@POST
	@Path("/{resourceId}/data")
	@Consumes({MediaType.APPLICATION_JSON})
	@Timed
	public void postData(@RestrictedTo(Authority.ROLE_USER) User visitor, @PathParam("userId") long userId, @PathParam("resourceId") long resourceId, DataPoint datapoint) {
		Resource resource = storage.findResourceById(resourceId);
		logger.info("Adding user/resource:"+resource.getLabel());
		//Resource resource = storage.findResourceById(resourceId);
		if (visitor.getId() != userId) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
	
	// add a resource 
	void insertResource(Resource resource) {
		storage.insertResource( 
			resource.getLabel(),
			resource.getVersion(), 
			resource.getOwner_id(), 
			resource.getParent_id(), 
			resource.getPolling_url(), 
			resource.getPolling_authentication_key(), 
			resource.getPolling_period(), 
			resource.getSecret_key(), 
			resource.getDescription(), 
			resource.getLast_polled(), 
			resource.getLast_posted() 
		);
	}

	// add a resource 
	void updateResource(long resourceId, Resource resource) {
		logger.info("Updating resourceID "+resourceId+" to: "+resource);
		//long parent_id = resource.getParent_id();
		//if (parent_id==0) {parent_id=null;}
		storage.updateResource( 
			resourceId, // the reosurce ID from the PUT'd URL
			resource.getLabel(),
			resource.getVersion(), 
			resource.getOwner_id(), 
			//resource.getParent_id(), 
			//parent_id,
			null,
			resource.getPolling_url(), 
			resource.getPolling_authentication_key(), 
			resource.getPolling_period(), 
			resource.getSecret_key(), 
			resource.getDescription(), 
			resource.getLast_polled(), 
			resource.getLast_posted() 
		);
		// remake pollers with updated Resource attribtues
		pollSystem.rebuildResourcePoller(resourceId);
	}
}

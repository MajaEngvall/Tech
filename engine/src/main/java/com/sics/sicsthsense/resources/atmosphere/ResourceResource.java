/*
 * Copyright (c) 2013, Swedish Institute of Computer Science
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *		 * Redistributions of source code must retain the above copyright
 *			 notice, this list of conditions and the following disclaimer.
 *		 * Redistributions in binary form must reproduce the above copyright
 *			 notice, this list of conditions and the following disclaimer in the
 *			 documentation and/or other materials provided with the distribution.
 *		 * Neither the name of The Swedish Institute of Computer Science nor the
 *			 names of its contributors may be used to endorse or promote products
 *			 derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SWEDISH INSTITUTE OF COMPUTER SCIENCE BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

/* Description: Jersey Resource for SicsthSense Resources. Handles config of Resources
 * contains the Parsers and Streams of the associated Resource. 
 * TODO:
 * */
package se.sics.sicsthsense.resources.atmosphere;

import java.util.List;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import com.yammer.dropwizard.auth.Auth;
import org.atmosphere.annotation.Broadcast;
import org.atmosphere.annotation.Suspend;

import se.sics.sicsthsense.Utils;
import se.sics.sicsthsense.core.*;
import se.sics.sicsthsense.jdbi.*;
import se.sics.sicsthsense.model.*;
import se.sics.sicsthsense.auth.*;
import se.sics.sicsthsense.auth.annotation.RestrictedTo;
import se.sics.sicsthsense.model.security.Authority;

// publicly reachable path of the resource
@Path("/{userId}/resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResourceResource {
	private final StorageDAO storage;
	private final AtomicLong counter;
	private PollSystem pollSystem;
	private final Logger logger = LoggerFactory.getLogger(ResourceResource.class);
	public ParseData parseData;
	List<Parser> parsers;

	// constructor with the system's stoarge and poll system.
	public ResourceResource() {
		this.storage = DAOFactory.getInstance();
		this.pollSystem = PollSystem.getInstance();
		this.counter = new AtomicLong();
		this.parseData = new ParseData();;
	}


	@GET
	@Timed
	public Response getResources(@PathParam("userId") long userId, @QueryParam("key") String key) {
		//logger.info("Getting all user "+userId+" resources for visitor "+visitor.toString());
		Utils.checkHierarchy(userId);
		User user = storage.findUserById(userId);
		if (user==null) {
			return Utils.resp(Status.NOT_FOUND, "Error: No userId match.", logger);
		}
		List<Resource> resources = storage.findResourcesByOwnerId(userId);
		if (!user.isAuthorised(key)) {
			return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger);
			/*
			Iterator<Resource> it = resources.iterator();
			while (it.hasNext()) {
				Resource r = it.next();
				if (r.) {it.remove();}
			}*/
		}
		return Utils.resp(Status.OK, resources, logger);
	}

	// resourceName can be the resourceID or the URL-encoded resource label
	@GET
	@Path("/{resourceId}")
	@Produces({MediaType.APPLICATION_JSON})
	@Timed
	public Response getResource(@PathParam("userId") long userId, @PathParam("resourceId") String resourceName, @QueryParam("key") String key) {
		logger.info("Getting user/resource: "+userId+"/"+resourceName);
		Utils.checkHierarchy(userId);
		Resource resource = Utils.findResourceByIdName(resourceName,userId);
		if (resource == null) { return Utils.resp(Status.NOT_FOUND, "Resource "+resourceName+" does not exist!", logger); }
		if (resource.getOwner_id() != userId) { return Utils.resp(Status.NOT_FOUND, "User "+userId+" does not own resource "+resourceName, logger); }
		User user = storage.findUserById(userId);
		if (user==null) {throw new WebApplicationException(Status.NOT_FOUND);}
		if (!user.isAuthorised(key) && !resource.isAuthorised(key)) { return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger); }
		/*
		if (!resource.isReadable(visitor)) {
			logger.warn("Resource "+resource.getId()+" is not readable to user "+visitor.getId());
//		throw new WebApplicationException(Status.FORBIDDEN);
		}*/
		return Utils.resp(Status.OK, resource, logger);
	}

	// post new resource definition 
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Timed
	public Response postResource( @PathParam("userId") long userId, Resource resource, @QueryParam("key") String key) {
		logger.info("Adding user/resource:"+resource.getLabel());
		Utils.checkHierarchy(userId);
		User user = storage.findUserById(userId);
		if (user==null) {throw new WebApplicationException(Status.NOT_FOUND);}
		if (!user.isAuthorised(key)) { return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger); }

		resource.setOwner_id(userId); // should know the owner
		long resourceId = Utils.insertResource(resource);
		ResourceLog rl = new ResourceLog(resource);
		Utils.insertResourceLog(rl);

		if (resource.getPolling_period() > 0) {
			// remake pollers with updated Resource attribtues
			pollSystem.rebuildResourcePoller(resourceId);
		}
		return Utils.resp(Status.OK, resourceId, logger);
	}

	// put updated resource definition 
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	@Timed
	@Path("/{resourceId}")
	public Response updateResource(@PathParam("userId") long userId, @PathParam("resourceId") String resourceName, Resource resource, @QueryParam("key") String key) {
		logger.info("Updating resourceName:"+resourceName);
		User user = storage.findUserById(userId);
		Resource oldresource = Utils.findResourceByIdName(resourceName,userId);
		Utils.checkHierarchy(user,oldresource);
		if (!user.isAuthorised(key) && !resource.isAuthorised(key)) { return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger); }
		Utils.updateResource(oldresource.getId(), resource);
		return Response.ok().build();
	}

	@DELETE
	@Timed
	@Path("/{resourceId}")
	public Response deleteResource(//@RestrictedTo(Authority.ROLE_USER) User visitor, 
			@PathParam("userId") long userId, @PathParam("resourceId") String resourceName, @QueryParam("key") String key) {
		logger.warn("Deleting resourceName:"+resourceName);
		User user = storage.findUserById(userId);
		Resource resource = Utils.findResourceByIdName(resourceName,userId);
		Utils.checkHierarchy(user,resource);
		if (!user.isAuthorised(key)) { return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger); }
		// delete child streams and parsers
		List<Stream> streams = storage.findStreamsByResourceId(resource.getId());
		List<Parser> parsers = storage.findParsersByResourceId(resource.getId());
		for (Parser p: parsers) {storage.deleteParser(p.getId());}
		for (Stream s: streams) {storage.deleteStream(s.getId());}
		storage.deleteResource(resource.getId());
		// TODO: should delete resource log
		// remake pollers with updated Resource attribtues
		pollSystem.rebuildResourcePoller(resource.getId());
		return Response.ok().build();
	}

	@GET
	@Path("/{resourceId}/data")
	public Response getData() {
		return Utils.resp(Status.FORBIDDEN, "Error: Only Streams can have data read", logger);
	}
	@GET
	@Path("/{resourceId}/rebuild")
	public Response rebuild(@PathParam("userId") long userId, @PathParam("resourceId") String resourceName) {
		Resource resource = Utils.findResourceByIdName(resourceName,userId);
		if (resource==null) {
			return Utils.resp(Status.NOT_FOUND, "Error: resource name does not exist: "+resourceName, logger);
		}
		pollSystem.rebuildResourcePoller(resource.getId());
		return Utils.resp(Status.OK, "Rebuilt: "+resourceName, logger);
	}

	// Post data to the resource, and run data through its parsers
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/{resourceId}/data")
	public Response postData(@PathParam("userId") long userId, @PathParam("resourceId") String resourceName, String data, @QueryParam("key") String key) {
		User user = storage.findUserById(userId);
		Resource resource = Utils.findResourceByIdName(resourceName);
		Utils.checkHierarchy(user,resource);
		if (!resource.isAuthorised(key) && !user.isAuthorised(key)) { return Utils.resp(Status.FORBIDDEN, "Error: Key does not match! "+key, logger); }
		//logger.info("Adding data to resource: "+resource.getLabel());

		// if parsers are undefined, create them!
		List<Parser> parsers = storage.findParsersByResourceId(resource.getId());
		if (parsers==null || parsers.size()==0) { 
			logger.info("No parsers defined! Trying to auto create for: "+resource.getLabel());
			try {
				// staticness is a mess...
				parseData.autoCreateJsonParsers(PollSystem.getInstance().mapper, resource, data); 
			} catch (Exception e) {
				return Utils.resp(Status.BAD_REQUEST, "Error: JSON parsing for auto creation failed!", logger);
			}
		}
		//run it through the parsers and update resource log
		applyParsers(resource.getId(), data);

		// update Resource last_posted
		storage.postedResource(resource.getId(),System.currentTimeMillis());

		return Utils.resp(Status.OK, "Success", logger);
	}

	public void applyParsers(long resourceId, String data) {
		boolean parsedSuccessfully=true;
		String parseError = "";
		logger.info("Applying all parsers to data: "+data);
		if (parsers==null) { parsers = storage.findParsersByResourceId(resourceId); }
		for (Parser parser: parsers) {
			//logger.info("applying a parser "+parser.getInput_parser());
			try {
				parseData.apply(parser,data);
			} catch (Exception e) {
				parsedSuccessfully=false;
				logger.error("Parsing "+data+" failed!"+e);
				parseError += "Parsing "+data+" failed!"+e;
			}
		}
		// append interaction to resource log!
		logger.info("Updating log!");
		ResourceLog rl = storage.findResourceLogByResourceId(resourceId);
		if (rl != null) {
			logger.error("ResourceLog does not exist!");
		}
		//TODO: update the actual log message!
		rl.update(parsedSuccessfully, false, "received POST", System.currentTimeMillis());
		rl.save();
	}

 
}

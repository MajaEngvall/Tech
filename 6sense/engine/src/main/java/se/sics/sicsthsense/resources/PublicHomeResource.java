/* Description: Simple template serving resource
 * TODO:
 * */
package se.sics.sicsthsense.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.dropwizard.jersey.caching.CacheControl;
import com.codahale.metrics.annotation.Timed;

import se.sics.sicsthsense.model.BaseModel;
import se.sics.sicsthsense.views.PublicFreemarkerView;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of configuration for public home page</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PublicHomeResource {

  /**
   * Provide the initial view on to the system
   *
   * @return A localised view containing HTML
   */
  @GET
  @Timed
  @CacheControl(noCache = true)
  public PublicFreemarkerView viewHome() {
    BaseModel model = new BaseModel();
    return new PublicFreemarkerView<BaseModel>("common/home.ftl",model);
  }

  /**
   * Provide the initial view on to the system
   *
   * @return A the favicon images from the assets
   */
  @GET
  @Path("favicon.ico")
  @Timed
  @CacheControl(maxAge = 24, maxAgeUnit = TimeUnit.HOURS)
  public Response viewFavicon() {
    InputStream is = PublicHomeResource.class.getResourceAsStream("/assets/favicon.ico");
    return Response.ok(is).build();
  }

}

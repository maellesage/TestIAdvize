package com.viedemerde.recherche.api;

import java.io.IOException;
import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.viedemerde.recherche.services.RechercherLesPostsService;

/**
 * 
 * @author Maelle LESAGE
 * 
 *         Classe pour rechercher les derniers posts
 *
 */
@Path("/posts")
public class PostVieDeMerdeApi {

	// Initialisation
	RechercherLesPostsService post = new RechercherLesPostsService();

	/**
	 * M�thode de recherche de tous les posts
	 * 
	 * @return Liste de posts � afficher
	 * @throws ParseException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON +"; charset=UTF-8")
	public Response findAll(@QueryParam("from") String from, @QueryParam("to") String to,
			@QueryParam("author") String author)
					throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		return post.findAll(from, to, author);
	}

	/**
	 * M�thode de recherche de posts par id
	 * 
	 * @param id
	 *            id du post
	 * @return Liste de posts � afficher
	 * @throws ParseException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON +"; charset=UTF-8")
	public Response findById(@PathParam("id") String id)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		return post.findById(id);
	}

}

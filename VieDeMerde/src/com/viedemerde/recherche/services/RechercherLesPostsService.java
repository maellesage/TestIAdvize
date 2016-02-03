package com.viedemerde.recherche.services;

import java.io.IOException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RechercherLesPostsService {

	public static final int nbPostsAttendus = 201;
	public static final int page = 16;
	public static final String ID = "id";
	public static final String CONTENT = "content";
	public static final String DATE = "date";
	public static final String AUTHOR = "author";
	public static final String URL = "http://www.viedemerde.fr/?page=";
	public static final String ELEMENT_LI_ID = "li[id]";
	public static final String POST = "post";
	public static final String POSTS = "posts";
	public static final String COUNT = "count";
	public static final String TEXT = "text";
	public static final String FORMAT_DATE_PARAMS = "yyyy-MM-dd";
	public static final String FORMAT_DATE_POST = "dd/MM/yyyy";
	public static final String ELEMENT_DATE = ".date";
	public static final String MALE = "male";
	public static final String FEMALE = "female";

	/**
	 * M�thode de recherche de posts
	 * 
	 * @param from
	 *            date de d�but
	 * @param to
	 *            date de fin
	 * @param author
	 *            auteur du post
	 * @return Liste de posts � afficher
	 * @throws ParseException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public Response findAll(String from, String to, String author)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		return find(from, to, author, null);
	}

	/**
	 * M�thode de recherche d'un post par son id
	 * 
	 * @param id
	 *            id du post
	 * @return Liste de posts � afficher
	 * @throws ParseException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public Response findById(String id)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		return find(null, null, null, id);
	}

	/**
	 * M�thode de recherche des posts
	 * 
	 * @param from
	 *            Date de d�but
	 * @param to
	 *            Date de fin
	 * @param author
	 *            auteur du post
	 * @param id
	 *            id du post
	 * @return Liste de posts � afficher
	 * @throws ParseException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public Response find(String from, String to, String author, String id)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {

		// Initialisation
		Integer nbPostsTotal = 0;
		Elements posts = new Elements();
		List<Map<String, Object>> listeMap = new ArrayList<Map<String, Object>>();
		Map<String, Object> mapPosts = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		String json = StringUtils.EMPTY;
		Map<String, Object> map = new HashMap<String, Object>();

		// Connexion au site et r�cup�ration des posts des 15 premi�res pages
		while (nbPostsTotal < page) {
			Document doc = null;
			try {
				// Connexion au site Vie De Merde sur 15 pages
				doc = Jsoup.connect(URL + nbPostsTotal).get();
			} catch (IOException e) {
				System.out.println("Problème lors de la connection à Vie De Merde!");
				e.printStackTrace();
			}
			
			/*
			 * R�cup�ration des �l�ments <li> qui correspond � un post dans
			 * une liste
			 */
			if(doc != null){
				posts.addAll(doc.select(ELEMENT_LI_ID));
			}

			nbPostsTotal++;
		}

		// Choix de la m�thode de recherche
		if (StringUtils.isBlank(from) && StringUtils.isBlank(to) && StringUtils.isBlank(author)
				&& StringUtils.isBlank(id)) {
			listeMap = findAllPosts(posts, map);
		} else if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
			listeMap = findByDate(posts, map, from, to);
		} else if (StringUtils.isNotBlank(author)) {
			listeMap = findByAuthor(posts, map, author);
		} else if (StringUtils.isNotBlank(id)) {
			listeMap = findByIdPost(posts, map, id);
		}

		if (StringUtils.isBlank(json) && (listeMap == null || listeMap.size() == 0)) {
			json = "Aucun résultat";
		} else if (StringUtils.isNotBlank(id)) {
			mapPosts.put(POST, listeMap.get(0));
		} else {
			mapPosts.put(COUNT, listeMap.size());
			mapPosts.put(POSTS, listeMap);
		}

		// Application du formattage pour l'affichage
		json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapPosts);

		// Envoi de la r�ponse
		return Response.status(200).entity(json).build();
	}

	/**
	 * M�thode de r�cup�ration de tous les posts
	 * 
	 * @param posts
	 *            posts � afficher
	 * @param map
	 *            liste � afficher
	 * @return liste � afficher
	 * @throws ParseException
	 *             En cas d'erreur
	 */
	public List<Map<String, Object>> findAllPosts(Elements posts, Map<String, Object> map) throws ParseException {

		// Initialisation
		List<Map<String, Object>> listeMap = new ArrayList<Map<String, Object>>();
		Integer nbPosts = 1;

		// Cr�ation de la liste des 200 dernier posts de Vie De Merde
		Iterator<Element> iterPosts = posts.iterator();
		while (iterPosts.hasNext() && nbPosts < nbPostsAttendus) {
			Element post = iterPosts.next();
			map = new HashMap<String, Object>();

			map.put(ID, nbPosts);
			map.put(CONTENT, post.getElementsByClass(TEXT).text());
			String date = post.select(ELEMENT_DATE).first().childNode(0).toString();
			map.put(DATE, formatDateToString(date, new SimpleDateFormat(FORMAT_DATE_POST)));
			map.put(AUTHOR, recupeAuthor(post));

			// Ajout
			listeMap.add(map);

			nbPosts++;
		}

		return listeMap;
	}

	/**
	 * M�thode de recherche de posts � partir d'une date de d�but et d'une date
	 * de fin
	 * 
	 * @param posts
	 *            liste des 200 derniers posts Vie De Merde
	 * @param map
	 *            Liste de posts � retourner
	 * @param from
	 *            date de d�but
	 * @param to
	 *            date de fin
	 * @return liste de posts
	 * @throws ParseException
	 *             En cas d'erreur
	 */
	public List<Map<String, Object>> findByDate(Elements posts, Map<String, Object> map, String from, String to)
			throws ParseException {

		// Initialisation
		List<Map<String, Object>> listeMap = new ArrayList<Map<String, Object>>();
		Integer nbPosts = 1;
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_PARAMS);
		Date fromPost = (Date) sdf.parse(from);
		Date toPost = (Date) sdf.parse(to);

		// Parcours des 200 derniers posts de Vie De Merde
		Iterator<Element> iterPosts = posts.iterator();
		while (iterPosts.hasNext() && nbPosts < nbPostsAttendus) {
			Element post = iterPosts.next();
			SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_POST);
			String date = post.select(ELEMENT_DATE).first().childNode(0).toString();
			Date datePost = (Date) formatter.parse(date);

			// R�cup�ration des posts dont la date est comprise entre les dates
			// mises en param�tres ou �gale
			if ((datePost.after(fromPost) && datePost.before(toPost)) || datePost.equals(fromPost)
					|| datePost.equals(toPost)) {
				map = new HashMap<String, Object>();
				map.put(ID, nbPosts);
				map.put(CONTENT, post.getElementsByClass(TEXT).text());
				map.put(DATE, formatter.format(datePost));
				map.put(AUTHOR, recupeAuthor(post));

				// Ajout
				listeMap.add(map);
			}

			nbPosts++;
		}

		return listeMap;
	}

	/**
	 * M�thode de r�cup�ration des posts par le nom d'auteur
	 * 
	 * @param posts
	 *            posts trouv�s
	 * @param map
	 *            posts a retourner
	 * @param author
	 *            auteur du post
	 * @return liste des posts � afficher
	 * @throws ParseException
	 *             En cas d'erreur
	 */
	public List<Map<String, Object>> findByAuthor(Elements posts, Map<String, Object> map, String author)
			throws ParseException {

		// Initialisation
		List<Map<String, Object>> listeMap = new ArrayList<Map<String, Object>>();
		Integer nbPosts = 1;

		// Parcours des 200 derniers posts de Vie De Merde
		Iterator<Element> iterPosts = posts.iterator();
		while (iterPosts.hasNext() && nbPosts < nbPostsAttendus) {

			Element post = iterPosts.next();
			String authorPosti = recupeAuthor(post);

			// Enregistrement des posts dont l'auteur correspond
			if(StringUtils.isNotBlank(authorPosti)){
				if (author.equals(authorPosti)) {
					map = new HashMap<String, Object>();
					map.put(ID, nbPosts);
					map.put(CONTENT, post.getElementsByClass(TEXT).text());
					String date = post.select(ELEMENT_DATE).first().childNode(0).toString();
					map.put(DATE, formatDateToString(date, new SimpleDateFormat(FORMAT_DATE_POST)));
					map.put(AUTHOR, authorPosti);
	
					listeMap.add(map);
				}
			}

			nbPosts++;
		}

		return listeMap;
	}

	/**
	 * M�thode de r�cup�ration des posts par ID
	 * 
	 * @param posts
	 *            liste de posts
	 * @param map
	 *            liste de posts sauvegard�s
	 * @param id
	 *            id du post
	 * @return liste de poste
	 * @throws ParseException
	 *             En cas d'erreur
	 */
	public List<Map<String, Object>> findByIdPost(Elements posts, Map<String, Object> map, String id)
			throws ParseException {

		// Initialisation
		List<Map<String, Object>> listeMap = new ArrayList<Map<String, Object>>();
		Integer idPost = Integer.valueOf(id);
		
		if(idPost > 200){
			return listeMap;
		}
		
		Element post = posts.get(idPost--);

		if (post != null) {
			map = new HashMap<String, Object>();
			map.put(ID, Integer.valueOf(id));
			map.put(CONTENT, post.getElementsByClass(TEXT).text());
			String date = post.select(ELEMENT_DATE).first().childNode(0).toString();
			map.put(DATE, formatDateToString(date, new SimpleDateFormat(FORMAT_DATE_POST)));
			map.put(AUTHOR, recupeAuthor(post));

			// Ajout du post
			listeMap.add(map);
		}

		return listeMap;
	}

	/**
	 * M�thode de formatage d'une date en String
	 * 
	 * @param date
	 *            date � formatter
	 * @param formatter
	 *            formattage
	 * @return date
	 * @throws ParseException
	 *             En cas d'erreur
	 */
	public String formatDateToString(String date, SimpleDateFormat formatter) throws ParseException {

		SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE_POST);
		String datePost = date;
		Date dateP = (Date) format.parse(datePost);
		return formatter.format(dateP);
	}

	/**
	 * M�thode de r�cup�ration de l'auteur d'un post
	 * 
	 * @param post
	 *            post de Vie De Merde
	 * @return auteur
	 */
	public String recupeAuthor(Element post) {

		String authorPost = null;
		if (post.getElementsByClass(MALE).text() != null && !"".equals(post.getElementsByClass(MALE).text())) {
			authorPost = post.getElementsByClass(MALE).text();
		} else if (post.getElementsByClass(FEMALE).text() != null && !"".equals(post.getElementsByClass(FEMALE).text())) {
			authorPost = post.getElementsByClass(FEMALE).text();
		} else {
			authorPost = post.select(ELEMENT_DATE).first().childNode(2).toString();
		}
		return authorPost;
	}

}

package org.openapitools.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.glassfish.jersey.logging.LoggingFeature;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import java.net.URLEncoder;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openapitools.client.auth.Authentication;
import org.openapitools.client.auth.HttpBasicAuth;
import org.openapitools.client.auth.HttpBearerAuth;
import org.openapitools.client.auth.ApiKeyAuth;
import org.openapitools.client.model.AbstractOpenApiSchema;

import org.openapitools.client.auth.OAuth;


public class ApiClient {
  protected Map<String, String> defaultHeaderMap = new HashMap<String, String>();
  protected Map<String, String> defaultCookieMap = new HashMap<String, String>();
  protected String basePath = "http://petstore.swagger.io:80/v2";
  protected List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>(Arrays.asList(
    new ServerConfiguration(
      "http://{server}.swagger.io:{port}/v2",
      "petstore server",
      new HashMap<String, ServerVariable>() {{
        put("server", new ServerVariable(
          "No description provided",
          "petstore",
          new HashSet<String>(
            Arrays.asList(
              "petstore",
              "qa-petstore",
              "dev-petstore"
            )
          )
        ));
        put("port", new ServerVariable(
          "No description provided",
          "80",
          new HashSet<String>(
            Arrays.asList(
              "80",
              "8080"
            )
          )
        ));
      }}
    ),
    new ServerConfiguration(
      "https://localhost:8080/{version}",
      "The local server",
      new HashMap<String, ServerVariable>() {{
        put("version", new ServerVariable(
          "No description provided",
          "v2",
          new HashSet<String>(
            Arrays.asList(
              "v1",
              "v2"
            )
          )
        ));
      }}
    )
  ));
  protected Integer serverIndex = 0;
  protected Map<String, String> serverVariables = null;
  protected Map<String, List<ServerConfiguration>> operationServers = new HashMap<String, List<ServerConfiguration>>() {{
    put("PetApi.addPet", new ArrayList<ServerConfiguration>(Arrays.asList(
      new ServerConfiguration(
        "http://petstore.swagger.io/v2",
        "No description provided",
        new HashMap<String, ServerVariable>()
      ),

      new ServerConfiguration(
        "http://path-server-test.petstore.local/v2",
        "No description provided",
        new HashMap<String, ServerVariable>()
      )
    )));
    put("PetApi.updatePet", new ArrayList<ServerConfiguration>(Arrays.asList(
      new ServerConfiguration(
        "http://petstore.swagger.io/v2",
        "No description provided",
        new HashMap<String, ServerVariable>()
      ),

      new ServerConfiguration(
        "http://path-server-test.petstore.local/v2",
        "No description provided",
        new HashMap<String, ServerVariable>()
      )
    )));
  }};
  protected Map<String, Integer> operationServerIndex = new HashMap<String, Integer>();
  protected Map<String, Map<String, String>> operationServerVariables = new HashMap<String, Map<String, String>>();
  protected boolean debugging = false;
  protected int connectionTimeout = 0;
  private int readTimeout = 0;

  protected Client httpClient;
  protected JSON json;
  protected String tempFolderPath = null;

  protected Map<String, Authentication> authentications;
  protected Map<String, String> authenticationLookup;

  protected DateFormat dateFormat;

  public ApiClient() {
    json = new JSON();
    httpClient = buildHttpClient(debugging);

    this.dateFormat = new RFC3339DateFormat();

    // Set default User-Agent.
    setUserAgent("OpenAPI-Generator/1.0.0/java");

    // Setup authentications (key: authentication name, value: authentication).
    authentications = new HashMap<String, Authentication>();
    authentications.put("api_key", new ApiKeyAuth("header", "api_key"));
    authentications.put("api_key_query", new ApiKeyAuth("query", "api_key_query"));
    authentications.put("bearer_test", new HttpBearerAuth("bearer"));
    authentications.put("http_basic_test", new HttpBasicAuth());
    authentications.put("http_signature", new HttpBearerAuth("scheme"));
    authentications.put("petstore_auth", new OAuth());
    // Prevent the authentications from being modified.
    authentications = Collections.unmodifiableMap(authentications);

    // Setup authentication lookup (key: authentication alias, value: authentication name)
    authenticationLookup = new HashMap<String, String>();
  }

  /**
   * Gets the JSON instance to do JSON serialization and deserialization.
   * @return JSON
   */
  public JSON getJSON() {
    return json;
  }

  public Client getHttpClient() {
    return httpClient;
  }

  public ApiClient setHttpClient(Client httpClient) {
    this.httpClient = httpClient;
    return this;
  }

  public String getBasePath() {
    return basePath;
  }

  public ApiClient setBasePath(String basePath) {
    this.basePath = basePath;
    return this;
  }

  public List<ServerConfiguration> getServers() {
    return servers;
  }

  public ApiClient setServers(List<ServerConfiguration> servers) {
    this.servers = servers;
    return this;
  }

  public Integer getServerIndex() {
    return serverIndex;
  }

  public ApiClient setServerIndex(Integer serverIndex) {
    this.serverIndex = serverIndex;
    return this;
  }

  public Map<String, String> getServerVariables() {
    return serverVariables;
  }

  public ApiClient setServerVariables(Map<String, String> serverVariables) {
    this.serverVariables = serverVariables;
    return this;
  }

  /**
   * Get authentications (key: authentication name, value: authentication).
   * @return Map of authentication object
   */
  public Map<String, Authentication> getAuthentications() {
    return authentications;
  }

  /**
   * Get authentication for the given name.
   *
   * @param authName The authentication name
   * @return The authentication, null if not found
   */
  public Authentication getAuthentication(String authName) {
    return authentications.get(authName);
  }

  /**
   * Helper method to set username for the first HTTP basic authentication.
   * @param username Username
   */
  public void setUsername(String username) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof HttpBasicAuth) {
        ((HttpBasicAuth) auth).setUsername(username);
        return;
      }
    }
    throw new RuntimeException("No HTTP basic authentication configured!");
  }

  /**
   * Helper method to set password for the first HTTP basic authentication.
   * @param password Password
   */
  public void setPassword(String password) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof HttpBasicAuth) {
        ((HttpBasicAuth) auth).setPassword(password);
        return;
      }
    }
    throw new RuntimeException("No HTTP basic authentication configured!");
  }

  /**
   * Helper method to set API key value for the first API key authentication.
   * @param apiKey API key
   */
  public void setApiKey(String apiKey) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof ApiKeyAuth) {
        ((ApiKeyAuth) auth).setApiKey(apiKey);
        return;
      }
    }
    throw new RuntimeException("No API key authentication configured!");
  }

  /**
   * Helper method to configure authentications which respects aliases of API keys.
   *
   * @param secrets Hash map from authentication name to its secret.
   */
  public void configureApiKeys(HashMap<String, String> secrets) {
    for (Map.Entry<String, Authentication> authEntry : authentications.entrySet()) {
      Authentication auth = authEntry.getValue();
      if (auth instanceof ApiKeyAuth) {
        String name = authEntry.getKey();
        // respect x-auth-id-alias property
        name = authenticationLookup.getOrDefault(name, name);
        if (secrets.containsKey(name)) {
          ((ApiKeyAuth) auth).setApiKey(secrets.get(name));
        }
      }
    }
  }

  /**
   * Helper method to set API key prefix for the first API key authentication.
   * @param apiKeyPrefix API key prefix
   */
  public void setApiKeyPrefix(String apiKeyPrefix) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof ApiKeyAuth) {
        ((ApiKeyAuth) auth).setApiKeyPrefix(apiKeyPrefix);
        return;
      }
    }
    throw new RuntimeException("No API key authentication configured!");
  }

  /**
   * Helper method to set bearer token for the first Bearer authentication.
   * @param bearerToken Bearer token
   */
  public void setBearerToken(String bearerToken) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof HttpBearerAuth) {
        ((HttpBearerAuth) auth).setBearerToken(bearerToken);
        return;
      }
    }
    throw new RuntimeException("No Bearer authentication configured!");
  }

  /**
   * Helper method to set access token for the first OAuth2 authentication.
   * @param accessToken Access token
   */
  public void setAccessToken(String accessToken) {
    for (Authentication auth : authentications.values()) {
      if (auth instanceof OAuth) {
        ((OAuth) auth).setAccessToken(accessToken);
        return;
      }
    }
    throw new RuntimeException("No OAuth2 authentication configured!");
  }

  /**
   * Set the User-Agent header's value (by adding to the default header map).
   * @param userAgent Http user agent
   * @return API client
   */
  public ApiClient setUserAgent(String userAgent) {
    addDefaultHeader("User-Agent", userAgent);
    return this;
  }

  /**
   * Add a default header.
   *
   * @param key The header's key
   * @param value The header's value
   * @return API client
   */
  public ApiClient addDefaultHeader(String key, String value) {
    defaultHeaderMap.put(key, value);
    return this;
  }

  /**
   * Add a default cookie.
   *
   * @param key The cookie's key
   * @param value The cookie's value
   * @return API client
   */
  public ApiClient addDefaultCookie(String key, String value) {
    defaultCookieMap.put(key, value);
    return this;
  }

  /**
   * Check that whether debugging is enabled for this API client.
   * @return True if debugging is switched on
   */
  public boolean isDebugging() {
    return debugging;
  }

  /**
   * Enable/disable debugging for this API client.
   *
   * @param debugging To enable (true) or disable (false) debugging
   * @return API client
   */
  public ApiClient setDebugging(boolean debugging) {
    this.debugging = debugging;
    // Rebuild HTTP Client according to the new "debugging" value.
    this.httpClient = buildHttpClient(debugging);
    return this;
  }

  /**
   * The path of temporary folder used to store downloaded files from endpoints
   * with file response. The default value is <code>null</code>, i.e. using
   * the system's default tempopary folder.
   *
   * @return Temp folder path
   */
  public String getTempFolderPath() {
    return tempFolderPath;
  }

  /**
   * Set temp folder path
   * @param tempFolderPath Temp folder path
   * @return API client
   */
  public ApiClient setTempFolderPath(String tempFolderPath) {
    this.tempFolderPath = tempFolderPath;
    return this;
  }

  /**
   * Connect timeout (in milliseconds).
   * @return Connection timeout
   */
  public int getConnectTimeout() {
    return connectionTimeout;
  }

  /**
   * Set the connect timeout (in milliseconds).
   * A value of 0 means no timeout, otherwise values must be between 1 and
   * {@link Integer#MAX_VALUE}.
   * @param connectionTimeout Connection timeout in milliseconds
   * @return API client
   */
  public ApiClient setConnectTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    httpClient.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
    return this;
  }

  /**
   * read timeout (in milliseconds).
   * @return Read timeout
   */
  public int getReadTimeout() {
    return readTimeout;
  }

  /**
   * Set the read timeout (in milliseconds).
   * A value of 0 means no timeout, otherwise values must be between 1 and
   * {@link Integer#MAX_VALUE}.
   * @param readTimeout Read timeout in milliseconds
   * @return API client
   */
  public ApiClient setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    httpClient.property(ClientProperties.READ_TIMEOUT, readTimeout);
    return this;
  }

  /**
   * Get the date format used to parse/format date parameters.
   * @return Date format
   */
  public DateFormat getDateFormat() {
    return dateFormat;
  }

  /**
   * Set the date format used to parse/format date parameters.
   * @param dateFormat Date format
   * @return API client
   */
  public ApiClient setDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
    // also set the date format for model (de)serialization with Date properties
    this.json.setDateFormat((DateFormat) dateFormat.clone());
    return this;
  }

  /**
   * Parse the given string into Date object.
   * @param str String
   * @return Date
   */
  public Date parseDate(String str) {
    try {
      return dateFormat.parse(str);
    } catch (java.text.ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Format the given Date object into string.
   * @param date Date
   * @return Date in string format
   */
  public String formatDate(Date date) {
    return dateFormat.format(date);
  }

  /**
   * Format the given parameter object into string.
   * @param param Object
   * @return Object in string format
   */
  public String parameterToString(Object param) {
    if (param == null) {
      return "";
    } else if (param instanceof Date) {
      return formatDate((Date) param);
    } else if (param instanceof Collection) {
      StringBuilder b = new StringBuilder();
      for(Object o : (Collection)param) {
        if(b.length() > 0) {
          b.append(',');
        }
        b.append(String.valueOf(o));
      }
      return b.toString();
    } else {
      return String.valueOf(param);
    }
  }

  /*
   * Format to {@code Pair} objects.
   * @param collectionFormat Collection format
   * @param name Name
   * @param value Value
   * @return List of pairs
   */
  public List<Pair> parameterToPairs(String collectionFormat, String name, Object value){
    List<Pair> params = new ArrayList<Pair>();

    // preconditions
    if (name == null || name.isEmpty() || value == null) return params;

    Collection valueCollection;
    if (value instanceof Collection) {
      valueCollection = (Collection) value;
    } else {
      params.add(new Pair(name, parameterToString(value)));
      return params;
    }

    if (valueCollection.isEmpty()){
      return params;
    }

    // get the collection format (default: csv)
    String format = (collectionFormat == null || collectionFormat.isEmpty() ? "csv" : collectionFormat);

    // create the params based on the collection format
    if ("multi".equals(format)) {
      for (Object item : valueCollection) {
        params.add(new Pair(name, parameterToString(item)));
      }

      return params;
    }

    String delimiter = ",";

    if ("csv".equals(format)) {
      delimiter = ",";
    } else if ("ssv".equals(format)) {
      delimiter = " ";
    } else if ("tsv".equals(format)) {
      delimiter = "\t";
    } else if ("pipes".equals(format)) {
      delimiter = "|";
    }

    StringBuilder sb = new StringBuilder() ;
    for (Object item : valueCollection) {
      sb.append(delimiter);
      sb.append(parameterToString(item));
    }

    params.add(new Pair(name, sb.substring(1)));

    return params;
  }

  /**
   * Check if the given MIME is a JSON MIME.
   * JSON MIME examples:
   *   application/json
   *   application/json; charset=UTF8
   *   APPLICATION/JSON
   *   application/vnd.company+json
   * "* / *" is also default to JSON
   * @param mime MIME
   * @return True if the MIME type is JSON
   */
  public boolean isJsonMime(String mime) {
    String jsonMime = "(?i)^(application/json|[^;/ \t]+/[^;/ \t]+[+]json)[ \t]*(;.*)?$";
    return mime != null && (mime.matches(jsonMime) || mime.equals("*/*"));
  }

  /**
   * Select the Accept header's value from the given accepts array:
   *   if JSON exists in the given array, use it;
   *   otherwise use all of them (joining into a string)
   *
   * @param accepts The accepts array to select from
   * @return The Accept header to use. If the given array is empty,
   *   null will be returned (not to set the Accept header explicitly).
   */
  public String selectHeaderAccept(String[] accepts) {
    if (accepts.length == 0) {
      return null;
    }
    for (String accept : accepts) {
      if (isJsonMime(accept)) {
        return accept;
      }
    }
    return StringUtil.join(accepts, ",");
  }

  /**
   * Select the Content-Type header's value from the given array:
   *   if JSON exists in the given array, use it;
   *   otherwise use the first one of the array.
   *
   * @param contentTypes The Content-Type array to select from
   * @return The Content-Type header to use. If the given array is empty,
   *   JSON will be used.
   */
  public String selectHeaderContentType(String[] contentTypes) {
    if (contentTypes.length == 0) {
      return "application/json";
    }
    for (String contentType : contentTypes) {
      if (isJsonMime(contentType)) {
        return contentType;
      }
    }
    return contentTypes[0];
  }

  /**
   * Escape the given string to be used as URL query value.
   * @param str String
   * @return Escaped string
   */
  public String escapeString(String str) {
    try {
      return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }

  /**
   * Serialize the given Java object into string entity according the given
   * Content-Type (only JSON is supported for now).
   * @param obj Object
   * @param formParams Form parameters
   * @param contentType Context type
   * @return Entity
   * @throws ApiException API exception
   */
  public Entity<?> serialize(Object obj, Map<String, Object> formParams, String contentType) throws ApiException {
    Entity<?> entity;
    if (contentType.startsWith("multipart/form-data")) {
      MultiPart multiPart = new MultiPart();
      for (Entry<String, Object> param: formParams.entrySet()) {
        if (param.getValue() instanceof File) {
          File file = (File) param.getValue();
          FormDataContentDisposition contentDisp = FormDataContentDisposition.name(param.getKey())
              .fileName(file.getName()).size(file.length()).build();
          multiPart.bodyPart(new FormDataBodyPart(contentDisp, file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        } else {
          FormDataContentDisposition contentDisp = FormDataContentDisposition.name(param.getKey()).build();
          multiPart.bodyPart(new FormDataBodyPart(contentDisp, parameterToString(param.getValue())));
        }
      }
      entity = Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
    } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
      Form form = new Form();
      for (Entry<String, Object> param: formParams.entrySet()) {
        form.param(param.getKey(), parameterToString(param.getValue()));
      }
      entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    } else {
      // We let jersey handle the serialization
      entity = Entity.entity(obj == null ? Entity.text("") : obj, contentType);
    }
    return entity;
  }

  public AbstractOpenApiSchema deserializeSchemas(Response response, AbstractOpenApiSchema schema) throws ApiException{

    Object result = null;
    int matchCounter = 0;
    ArrayList<String> matchSchemas = new ArrayList<>();

    for (Map.Entry<String, GenericType> entry : schema.getSchemas().entrySet()) {
      String schemaName = entry.getKey();
      GenericType schemaType = entry.getValue();

      if (schemaType instanceof GenericType) { // model
        try {
          Object deserializedObject = deserialize(response, schemaType);
          if (deserializedObject != null) {
            result = deserializedObject;
            matchCounter++;

            if ("anyOf".equals(schema.getSchemaType())) {
              break;
            } else if ("oneOf".equals(schema.getSchemaType())) {
              matchSchemas.add(schemaName);
            } else {
              throw new ApiException("Unknowe type found while expecting anyOf/oneOf:" + schema.getSchemaType());
            }
          } else {
            // failed to deserialize the response in the schema provided, proceed to the next one if any
          }
        } catch (Exception ex) {
          // failed to deserialize, do nothing and try next one (schema)
        }
      } else {// unknown type
        throw new ApiException(schemaType.getClass() + " is not a GenericType and cannot be handled properly in deserialization.");
      }

    }

    if (matchCounter > 1 && "oneOf".equals(schema.getSchemaType())) {// more than 1 match for oneOf
      throw new ApiException("Response body is invalid as it matches more than one schema (" + String.join(", ", matchSchemas) + ") defined in the oneOf model: " + schema.getClass().getName());
    } else if (matchCounter == 0) { // fail to match any in oneOf/anyOf schemas
      throw new ApiException("Response body is invalid as it doens't match any schemas (" + String.join(", ", schema.getSchemas().keySet()) + ") defined in the oneOf/anyOf model: " + schema.getClass().getName());
    } else { // only one matched
      schema.setActualInstance(result);
      return schema;
    }

  }



  /**
   * Deserialize response body to Java object according to the Content-Type.
   * @param <T> Type
   * @param response Response
   * @param returnType Return type
   * @return Deserialize object
   * @throws ApiException API exception
   */
  @SuppressWarnings("unchecked")
  public <T> T deserialize(Response response, GenericType<T> returnType) throws ApiException {
    if (response == null || returnType == null) {
      return null;
    }

    if ("byte[]".equals(returnType.toString())) {
      // Handle binary response (byte array).
      return (T) response.readEntity(byte[].class);
    } else if (returnType.getRawType() == File.class) {
      // Handle file downloading.
      T file = (T) downloadFileFromResponse(response);
      return file;
    }

    String contentType = null;
    List<Object> contentTypes = response.getHeaders().get("Content-Type");
    if (contentTypes != null && !contentTypes.isEmpty())
      contentType = String.valueOf(contentTypes.get(0));

    // read the entity stream multiple times
    response.bufferEntity();

    return response.readEntity(returnType);
  }

  /**
   * Download file from the given response.
   * @param response Response
   * @return File
   * @throws ApiException If fail to read file content from response and write to disk
   */
  public File downloadFileFromResponse(Response response) throws ApiException {
    try {
      File file = prepareDownloadFile(response);
      Files.copy(response.readEntity(InputStream.class), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return file;
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  public File prepareDownloadFile(Response response) throws IOException {
    String filename = null;
    String contentDisposition = (String) response.getHeaders().getFirst("Content-Disposition");
    if (contentDisposition != null && !"".equals(contentDisposition)) {
      // Get filename from the Content-Disposition header.
      Pattern pattern = Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
      Matcher matcher = pattern.matcher(contentDisposition);
      if (matcher.find())
        filename = matcher.group(1);
    }

    String prefix;
    String suffix = null;
    if (filename == null) {
      prefix = "download-";
      suffix = "";
    } else {
      int pos = filename.lastIndexOf('.');
      if (pos == -1) {
        prefix = filename + "-";
      } else {
        prefix = filename.substring(0, pos) + "-";
        suffix = filename.substring(pos);
      }
      // File.createTempFile requires the prefix to be at least three characters long
      if (prefix.length() < 3)
        prefix = "download-";
    }

    if (tempFolderPath == null)
      return File.createTempFile(prefix, suffix);
    else
      return File.createTempFile(prefix, suffix, new File(tempFolderPath));
  }

  /**
   * Invoke API by sending HTTP request with the given options.
   *
   * @param <T> Type
   * @param operation The qualified name of the operation
   * @param path The sub-path of the HTTP URL
   * @param method The request method, one of "GET", "POST", "PUT", "HEAD" and "DELETE"
   * @param queryParams The query parameters
   * @param body The request body object
   * @param headerParams The header parameters
   * @param cookieParams The cookie parameters
   * @param formParams The form parameters
   * @param accept The request's Accept header
   * @param contentType The request's Content-Type header
   * @param authNames The authentications to apply
   * @param returnType The return type into which to deserialize the response
   * @param schema An instance of the response that uses oneOf/anyOf
   * @return The response body in type of string
   * @throws ApiException API exception
   */
  public <T> ApiResponse<T> invokeAPI(String operation, String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams, Map<String, String> cookieParams, Map<String, Object> formParams, String accept, String contentType, String[] authNames, GenericType<T> returnType, AbstractOpenApiSchema schema) throws ApiException {

    // Not using `.target(targetURL).path(path)` below,
    // to support (constant) query string in `path`, e.g. "/posts?draft=1"
    String targetURL;
    if (serverIndex != null) {
      Integer index;
      List<ServerConfiguration> serverConfigurations;
      Map<String, String> variables;

      if (operationServers.containsKey(operation)) {
        index = operationServerIndex.getOrDefault(operation, serverIndex);
        variables = operationServerVariables.getOrDefault(operation, serverVariables);
        serverConfigurations = operationServers.get(operation);
      } else {
        index = serverIndex;
        variables = serverVariables;
        serverConfigurations = servers;
      }
      if (index < 0 || index >= serverConfigurations.size()) {
        throw new ArrayIndexOutOfBoundsException(String.format(
          "Invalid index %d when selecting the host settings. Must be less than %d", index, serverConfigurations.size()
        ));
      }
      targetURL = serverConfigurations.get(index).URL(variables) + path;
    } else {
      targetURL = this.basePath + path;
    }
    WebTarget target = httpClient.target(targetURL);

    if (queryParams != null) {
      for (Pair queryParam : queryParams) {
        if (queryParam.getValue() != null) {
          target = target.queryParam(queryParam.getName(), escapeString(queryParam.getValue()));
        }
      }
    }

    Invocation.Builder invocationBuilder = target.request().accept(accept);

    for (Entry<String, String> entry : headerParams.entrySet()) {
      String value = entry.getValue();
      if (value != null) {
        invocationBuilder = invocationBuilder.header(entry.getKey(), value);
      }
    }

    for (Entry<String, String> entry : cookieParams.entrySet()) {
      String value = entry.getValue();
      if (value != null) {
        invocationBuilder = invocationBuilder.cookie(entry.getKey(), value);
      }
    }

    for (Entry<String, String> entry : defaultCookieMap.entrySet()) {
      String value = entry.getValue();
      if (value != null) {
        invocationBuilder = invocationBuilder.cookie(entry.getKey(), value);
      }
    }

    for (Entry<String, String> entry : defaultHeaderMap.entrySet()) {
      String key = entry.getKey();
      if (!headerParams.containsKey(key)) {
        String value = entry.getValue();
        if (value != null) {
          invocationBuilder = invocationBuilder.header(key, value);
        }
      }
    }

    Entity<?> entity = serialize(body, formParams, contentType);
    
    updateParamsForAuth(authNames, queryParams, headerParams, cookieParams, method, path);

    Response response = null;

    try {
      if ("GET".equals(method)) {
        response = invocationBuilder.get();
      } else if ("POST".equals(method)) {
        response = invocationBuilder.post(entity);
      } else if ("PUT".equals(method)) {
        response = invocationBuilder.put(entity);
      } else if ("DELETE".equals(method)) {
        response = invocationBuilder.method("DELETE", entity);
      } else if ("PATCH".equals(method)) {
        response = invocationBuilder.method("PATCH", entity);
      } else if ("HEAD".equals(method)) {
        response = invocationBuilder.head();
      } else if ("OPTIONS".equals(method)) {
        response = invocationBuilder.options();
      } else if ("TRACE".equals(method)) {
        response = invocationBuilder.trace();
      } else {
        throw new ApiException(500, "unknown method type " + method);
      }

      int statusCode = response.getStatusInfo().getStatusCode();
      Map<String, List<String>> responseHeaders = buildResponseHeaders(response);

      if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
        return new ApiResponse<>(statusCode, responseHeaders);
      } else if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
        if (returnType == null)
          return new ApiResponse<>(statusCode, responseHeaders);
        else
          if (schema == null) {
            return new ApiResponse<>(statusCode, responseHeaders, deserialize(response, returnType));
          } else { // oneOf/anyOf
            return new ApiResponse<>(statusCode, responseHeaders, (T)deserializeSchemas(response, schema));
          }
      } else {
        String message = "error";
        String respBody = null;
        if (response.hasEntity()) {
          try {
            respBody = String.valueOf(response.readEntity(String.class));
            message = respBody;
          } catch (RuntimeException e) {
            // e.printStackTrace();
          }
        }
        throw new ApiException(
          response.getStatus(),
          message,
          buildResponseHeaders(response),
          respBody);
      }
    } finally {
      try {
        response.close();
      } catch (Exception e) {
        // it's not critical, since the response object is local in method invokeAPI; that's fine, just continue
      }
    }
  }

  /**
   * @deprecated Add qualified name of the operation as a first parameter.
   */
  @Deprecated
  public <T> ApiResponse<T> invokeAPI(String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams, Map<String, String> cookieParams, Map<String, Object> formParams, String accept, String contentType, String[] authNames, GenericType<T> returnType, AbstractOpenApiSchema schema) throws ApiException {
    return invokeAPI(null, path, method, queryParams, body, headerParams, cookieParams, formParams, accept, contentType, authNames, returnType, schema);
  }

  /**
   * Build the Client used to make HTTP requests.
   * @param debugging Debug setting
   * @return Client
   */
  protected Client buildHttpClient(boolean debugging) {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    clientConfig.register(json);
    clientConfig.register(JacksonFeature.class);
    clientConfig.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    // turn off compliance validation to be able to send payloads with DELETE calls
    clientConfig.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
    if (debugging) {
      clientConfig.register(new LoggingFeature(java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), java.util.logging.Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 1024*50 /* Log payloads up to 50K */));
      clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY);
      // Set logger to ALL
      java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME).setLevel(java.util.logging.Level.ALL);
    } else {
      // suppress warnings for payloads with DELETE calls:
      java.util.logging.Logger.getLogger("org.glassfish.jersey.client").setLevel(java.util.logging.Level.SEVERE);
    }
    performAdditionalClientConfiguration(clientConfig);
    return ClientBuilder.newClient(clientConfig);
  }

  protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
    // No-op extension point
  }

  protected Map<String, List<String>> buildResponseHeaders(Response response) {
    Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
    for (Entry<String, List<Object>> entry: response.getHeaders().entrySet()) {
      List<Object> values = entry.getValue();
      List<String> headers = new ArrayList<String>();
      for (Object o : values) {
        headers.add(String.valueOf(o));
      }
      responseHeaders.put(entry.getKey(), headers);
    }
    return responseHeaders;
  }

  /**
   * Update query and header parameters based on authentication settings.
   *
   * @param authNames The authentications to apply
   * @param queryParams List of query parameters
   * @param headerParams Map of header parameters
   * @param cookieParams Map of cookie parameters
   * @param method HTTP method (e.g. POST)
   * @param uri HTTP URI
   */
  protected void updateParamsForAuth(String[] authNames, List<Pair> queryParams, Map<String, String> headerParams, Map<String, String> cookieParams, String method, String uri) throws ApiException {
    for (String authName : authNames) {
      Authentication auth = authentications.get(authName);
      if (auth == null) throw new RuntimeException("Authentication undefined: " + authName);
      auth.applyToParams(queryParams, headerParams, cookieParams, method, uri);
    }
  }
}

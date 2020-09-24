package com.mysite.core.models;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Sling Model for Unsplash Integration. This model is responsible for fetching all image related information that
 * author has configured via dialog.
 */
@Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
public class UnsplashModel {

    /**
     * List of Image Configurations
     */
    private final List<String> items = new ArrayList<>();

    public List<String> getItems() {
        return items;
    }

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private SlingHttpServletRequest request;

    @PostConstruct
    public void activate() throws URISyntaxException {

        final String fileReference = String.valueOf(currentResource.getValueMap().get("fileReference"));
        String uri = fileReference.split("\\?")[0];
        final Map<String, String> queryParams = getQueryParamsMap(fileReference);
        final Resource resource = currentResource.getChild("unsplash");
        if (resource != null) {
            final Iterable<Resource> resourceChildren = resource.getChildren();
            for(Resource res: resourceChildren) {
                final ValueMap valueMap = res.getValueMap();
                queryParams.put("fm", String.valueOf(valueMap.get("format")));
                queryParams.put("h", String.valueOf(valueMap.get("height")));
                queryParams.put("fit", String.valueOf(valueMap.get("fit")));
                queryParams.put("crop", String.valueOf(valueMap.get("crop")));
                queryParams.put("w", String.valueOf(valueMap.get("width")));
                queryParams.put("q", String.valueOf(valueMap.get("quality")));
                final URI buildUrl = buildUrl(uri, queryParams);
                items.add(buildUrl.toString());
            }
        }
    }

    private URI buildUrl(final String uri, final Map<String, String> queryParams) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(uri);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String,String> query : queryParams.entrySet()) //using map.entrySet() for iteration
        {
            BasicNameValuePair nameValuePair = new BasicNameValuePair(query.getKey(), query.getValue());
            nameValuePairs.add(nameValuePair);
        }
        return uriBuilder.addParameters(nameValuePairs).build();
    }

    private Map<String, String> getQueryParamsMap(String fileReference) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(fileReference), Charset.forName("UTF-8"));
        final Map<String, String> queryParams = new HashMap<>();
        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }
        return queryParams;
    }
}
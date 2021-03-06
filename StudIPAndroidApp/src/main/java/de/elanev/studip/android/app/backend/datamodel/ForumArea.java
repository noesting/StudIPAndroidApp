package de.elanev.studip.android.app.backend.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author joern
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumArea {
  public static final String ID = ForumArea.class.getName() + ".id";
  public static final String TITLE = ForumArea.class.getName() + ".title";

  @JsonProperty("anonymous")
  public int anonymous;
  @JsonProperty("chdate")
  public long chdate;
  @JsonProperty("content")
  public String content;
  @JsonProperty("content_html")
  public String contentHtml;
  @JsonProperty("depth")
  public int depth;
  @JsonProperty("mkdate")
  public long mkdate;
  @JsonProperty("new")
  public boolean isNew;
  @JsonProperty("new_children")
  public int newChildren;
  @JsonProperty("seminar_id")
  public String seminarId;
  @JsonProperty("subject")
  public String subject;
  @JsonProperty("topic_id")
  public String topicId;
  @JsonProperty("user_id")
  public String userId;
}

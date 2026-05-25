package io.translaas.client.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.translaas.client.TestApiUrls;
import io.translaas.client.TranslaasOptions;
import io.translaas.client.TranslaasRequestContext;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslaasHttpTest {

  @Mock private HttpResponse<String> response;

  @Mock private HttpInvoker httpInvoker;

  @Mock private HttpHeaders responseHeaders;

  private TranslaasOptions options;
  private TranslaasHttp http;

  @BeforeEach
  void setUp() {
    options =
        TranslaasOptions.builder()
            .apiKey("secret-key")
            .baseUrl(TestApiUrls.ORIGIN)
            .apiKeyHeader("X-Api-Key")
            .build();
    http = new TranslaasHttp(options, httpInvoker);
  }

  @Test
  void get_sendsApiKeyHeaderAndEncodesQuery() throws Exception {
    when(response.statusCode()).thenReturn(200);
    when(httpInvoker.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    Map<String, String> query = new HashMap<>();
    query.put("locale", "en");
    query.put("q", "hello world");

    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    http.get("/sdk/v1/translations/text", query, null);

    verify(httpInvoker).send(captor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest sent = captor.getValue();
    assertThat(sent.method()).isEqualTo("GET");
    assertThat(sent.headers().firstValue("X-Api-Key")).hasValue("secret-key");
    assertThat(sent.uri().getRawQuery()).contains("locale=en");
    assertThat(sent.uri().getRawQuery()).contains("q=hello+world");
    assertThat(sent.uri().getPath()).isEqualTo("/sdk/v1/translations/text");
  }

  @Test
  void get_addsIfNoneMatchWhenConditionalRequestsEnabled() throws Exception {
    when(response.statusCode()).thenReturn(200);
    when(response.headers()).thenReturn(responseHeaders);
    when(responseHeaders.firstValue("ETag")).thenReturn(Optional.empty());
    when(httpInvoker.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

    TranslaasRequestContext ctx = new TranslaasRequestContext();
    ctx.setIfNoneMatch("\"etag-1\"");
    http.get("/sdk/v1/translations/text", Map.of(), ctx);

    verify(httpInvoker).send(captor.capture(), any(HttpResponse.BodyHandler.class));
    HttpRequest sent = captor.getValue();
    assertThat(sent.headers().firstValue("If-None-Match")).hasValue("\"etag-1\"");
  }

  @Test
  void get_mergesSnapshotVersionAsQueryParamV() throws Exception {
    when(response.statusCode()).thenReturn(200);
    when(httpInvoker.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);

    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

    TranslaasOptions withV =
        TranslaasOptions.builder()
            .apiKey("k")
            .baseUrl(TestApiUrls.ORIGIN)
            .snapshotVersion("42")
            .build();
    TranslaasHttp h = new TranslaasHttp(withV, httpInvoker);
    h.get("/x", Map.of(), null);

    verify(httpInvoker).send(captor.capture(), any(HttpResponse.BodyHandler.class));
    assertThat(captor.getValue().uri().getRawQuery()).contains("v=42");
  }
}

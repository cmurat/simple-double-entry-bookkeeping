package per.cmurat.other.revolut.functest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import per.cmurat.other.revolut.core.rest.dto.AssetAccountDto;
import per.cmurat.other.revolut.core.rest.dto.TransactionDto;

import java.io.IOException;
import java.math.BigDecimal;

public class AccountingClient {
    private static final String BASE_URL = "http://localhost:4567";
    private static final ObjectMapper om = new ObjectMapper();

    public Response<AssetAccountDto> getAccount(final long accountId) throws IOException {
        final HttpGet getAccount = new HttpGet(BASE_URL + "/accounting/account/" + accountId);
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            final CloseableHttpResponse response = httpClient.execute(getAccount);
            final AssetAccountDto dto;
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity());
                dto = om.readValue(result, AssetAccountDto.class);
            } else {
                dto = null;
            }

            return new Response<>(dto, response);
        }
    }

    public Response<AssetAccountDto> createAccount(final BigDecimal balance) throws IOException {
        final HttpPut createAccount = new HttpPut(BASE_URL + "/accounting/account");

        final AssetAccountDto requestDto = new AssetAccountDto();
        requestDto.setBalance(balance);

        createAccount.setEntity(new StringEntity(om.writeValueAsString(requestDto)));
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            final CloseableHttpResponse response = httpClient.execute(createAccount);
            final AssetAccountDto dto;
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity());
                dto = om.readValue(result, AssetAccountDto.class);
            } else {
                dto = null;
            }

            return new Response<>(dto, response);
        }
    }

    public Response<Void> validateTransfer(final long sendingAccountId, final long receivingAccountId, final BigDecimal amount) throws IOException {
        final HttpPost validateTransfer = new HttpPost(BASE_URL + "/accounting/validateTransfer");

        final TransactionDto requestDto = new TransactionDto();
        requestDto.setSendingAccountId(sendingAccountId);
        requestDto.setReceivingAccountId(receivingAccountId);
        requestDto.setAmount(amount);

        validateTransfer.setEntity(new StringEntity(om.writeValueAsString(requestDto)));
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            final CloseableHttpResponse response = httpClient.execute(validateTransfer);
            return new Response<>(null, response);
        }
    }

    public Response<TransactionDto> processTransfer(final long sendingAccountId, final long receivingAccountId, final BigDecimal amount) throws IOException {
        final HttpPost processTransfer = new HttpPost(BASE_URL + "/accounting/processTransfer");

        final TransactionDto requestDto = new TransactionDto();
        requestDto.setSendingAccountId(sendingAccountId);
        requestDto.setReceivingAccountId(receivingAccountId);
        requestDto.setAmount(amount);

        processTransfer.setEntity(new StringEntity(om.writeValueAsString(requestDto)));
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            final CloseableHttpResponse response = httpClient.execute(processTransfer);
            final TransactionDto dto;
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity());
                dto = om.readValue(result, TransactionDto.class);
            } else {
                dto = null;
            }

            return new Response<>(dto, response);
        }
    }

    static class Response<T> {
        private final T parsedObject;
        private final CloseableHttpResponse response;

        Response(final T parsedObject, final CloseableHttpResponse response) {
            this.parsedObject = parsedObject;
            this.response = response;
        }

        public T getParsedObject() {
            return parsedObject;
        }

        public CloseableHttpResponse getResponse() {
            return response;
        }
    }
}

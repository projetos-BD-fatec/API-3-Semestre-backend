package br.com.bughunters.fusexflow.service;

import br.com.bughunters.fusexflow.dto.RequestResponse;
import br.com.bughunters.fusexflow.mock.RequestMock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {
    public List<RequestResponse> findAll() {
        return RequestMock.findAll();
    }
}

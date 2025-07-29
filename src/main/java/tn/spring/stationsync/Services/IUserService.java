package tn.spring.stationsync.Services;

import tn.spring.stationsync.Dtos.RegisterRequest;


public interface IUserService {

    void register(RegisterRequest request);
}

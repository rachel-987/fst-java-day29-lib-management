package fstr_java.day29.lib_management.service;

import fstr_java.day29.lib_management.dto.request.CreateMemberRequest;
import fstr_java.day29.lib_management.dto.response.MemberResponse;

import java.util.List;

public interface MemberService {
    MemberResponse createMember(CreateMemberRequest request);

    MemberResponse getMemberById(Long id);

    List<MemberResponse> getAllMembers();

}

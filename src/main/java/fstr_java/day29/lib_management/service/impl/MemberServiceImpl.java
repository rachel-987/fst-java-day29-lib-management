package fstr_java.day29.lib_management.service.impl;

import fstr_java.day29.lib_management.dto.request.CreateMemberRequest;
import fstr_java.day29.lib_management.dto.response.MemberResponse;
import fstr_java.day29.lib_management.entity.Member;
import fstr_java.day29.lib_management.exception.BusinessException;
import fstr_java.day29.lib_management.repository.MemberRepository;
import fstr_java.day29.lib_management.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    "Email already exists",
                    HttpStatus.CONFLICT
            );
        }

        Member member = new Member();
        member.setName(request.name());
        member.setEmail(request.email());

        Member savedMember = memberRepository.save(member);

        return toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Member not found with id: " + id,
                        HttpStatus.NOT_FOUND
                ));

        return toResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {

        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
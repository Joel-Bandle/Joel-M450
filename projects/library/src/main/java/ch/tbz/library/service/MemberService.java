package ch.tbz.library.service;

import ch.tbz.library.entity.Member;
import ch.tbz.library.exception.NotFoundException;
import ch.tbz.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member add(String name, String email) {
        return memberRepository.save(new Member(name, email));
    }

    public Member get(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mitglied " + id + " nicht gefunden"));
    }
}

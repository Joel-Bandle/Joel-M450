package ch.tbz.library.controller;

import ch.tbz.library.dto.MemberRequest;
import ch.tbz.library.entity.Member;
import ch.tbz.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member add(@Valid @RequestBody MemberRequest request) {
        return memberService.add(request.name(), request.email());
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable Long id) {
        return memberService.get(id);
    }
}

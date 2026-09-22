# Your task

This coding challenge asks you to work with an existing application and extend it
to add some new features and address bugs whilst preserving existing features. The
application, `claimline`, is an expense claim service. There are four tasks listed below.

This challenge is not meant to be a production ready version, but it is meant to show
thought process, concepts, and problem solving skills. You are free to use any 3rd party
libraries or code to accomplish the task as long as it is licensed accordingly.

You may use AI tools for this assessment, and are encouraged to do so. If you choose to use
an AI tool, please tell us how you used one and be prepared to discuss that in the interview
following this submission.

We recommend that you spend only 2-3 hours on the challenge, we are not looking for a
finished/polished solution, but rather an example of how you approach a problem, and a starter
for a conversation about a technical topic and the development process. Please work through
the tasks in order, completing as many as you have time for.

`README.md` covers what the service does and how to build and run it. Start
there.

---

## Task 1: more approvals on larger claims

Finance want more eyes on larger expense claims. At the moment a claim takes
exactly one approval: an approver can approve anything up to their personal
limit, and the claim is approved the moment they do.

### What we need

Approvals should go in steps, by the size of the claim:

| Claim amount        | Approvals needed |
| ------------------- | ---------------- |
| under $1,000        | 1                |
| $1,000 to $9,999    | 2                |
| $10,000 and above   | 3                |

A claim that has some but not all of its approvals stays pending. Below $1,000
one approval is still enough, and nothing about those claims should change.

**Reading a claim should show every approval it has received** — not just the
last one — and for each approval, **who gave it and when**. Finance get asked to
evidence individual sign-offs when the year is audited, so an approval that
cannot be traced to a person and a time is not much use to them.

Finance also want to see which claims are waiting on another approval, so they
can go and chase them.

The thresholds are owned by finance and they will want to change them, so they
should be changeable without editing code.

How you do it is up to you.

### What must not break

Treat the service as if it is used in production.

- Reports from previous months should show the same tallies from
  `GET /reports/monthly` after your changes. New claims need to be reported accurately
  too: each approved claim counts once, for its own amount, against its own category.
- Existing records in `data/audit-log.txt` need to stay readable.
- Assume there is a separately maintained mobile app that consumes `GET /claims/{id}`,
  which will not have a new release for six months.

---

## Task 2: April's equipment spend

Finance cannot close April. `GET /reports/monthly?month=2026-04` puts equipment
spend at over **two million dollars**. Every other month comes out in the low
thousands, and finance cannot tie the figure back to any claim they remember
approving.

Investigate where that spend is coming from. If there is a bug, fix it. Tell us
what you found.

---

## Task 3: a page showing claims awaiting approval

Finance would like something they can open each morning that lists the claims
waiting on another approval, so they can chase them.

---

## Task 4: withdrawing an approval

Approvers sometimes change their mind, or approve the wrong claim. An approver
should be able to withdraw an approval they gave, as long as the claim has not
yet been fully approved.

Withdrawal for fully approved claims is out of scope.

---

## When you are done

Provide your submission, together with a brief note covering:
- the design decisions you made
- the requirements you inferred
- the root cause of task 2
- whether and how you used AI for this task
- anything you noticed but chose not to fix, and what you would do about it next

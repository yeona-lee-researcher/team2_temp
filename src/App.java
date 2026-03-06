import java.util.List;
import java.util.Scanner;

/**
 * 애플리케이션 메인 실행 로직
 * 명령어 처리 및 프로그램 흐름 관리
 */
public class App {
    private Scanner scanner;
    private boolean running;
    
    // Repository 인스턴스들
    private ComicRepository comicRepository;
    private MemberRepository memberRepository;
    private RentalRepository rentalRepository;
    
    public App() {
        this.scanner = new Scanner(System.in);
        this.running = true;
        
        // Repository 초기화
        this.comicRepository = new ComicRepository();
        this.memberRepository = new MemberRepository();
        this.rentalRepository = new RentalRepository();
    }
    
    /**
     * 메인 실행 루프
     */
    public void run() {
        System.out.println("프로그램이 시작되었습니다.");
        
        // DB 연결 테스트
        if (!DBUtil.testConnection()) {
            System.out.println("데이터베이스 연결에 실패했습니다. 설정을 확인해주세요.");
            System.out.println("db.properties 파일의 DB 설정을 확인하고, MySQL 서버가 실행 중인지 확인해주세요.");
            return;
        }
        
        System.out.println("데이터베이스 연결 성공!");
        System.out.println("'help'를 입력하면 명령어 목록을 확인할 수 있습니다.");
        
        while (running) {
            System.out.print("\n명령어: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            Rq rq = new Rq(input);
            processCommand(rq);
        }
        
        scanner.close();
    }
    
    /**
     * 명령어 처리
     */
    private void processCommand(Rq rq) {
        String commandName = rq.getCommand();
        
        switch (commandName) {
            case "exit":
                handleExit();
                break;
            case "help":
                showHelp();
                break;
            case "comic-add":
                addComic();
                break;
            case "comic-list":
                listComics();
                break;
            case "comic-detail":
                showComicDetail(rq);
                break;
            case "comic-update":
                updateComic(rq);
                break;
            case "comic-delete":
                deleteComic(rq);
                break;
            case "member-add":
                addMember();
                break;
            case "member-list":
                listMembers();
                break;
            case "rent":
                rentComic(rq);
                break;
            case "return":
                returnComic(rq);
                break;
            case "rental-list":
                listRentals(rq);
                break;
            default:
                System.out.println("알 수 없는 명령어입니다. 'help'를 입력해주세요.");
                break;
        }
    }
    
    /**
     * 프로그램 종료 처리
     */
    private void handleExit() {
        System.out.println("프로그램을 종료합니다.");
        running = false;
    }
    
    /**
     * 도움말 출력
     */
    private void showHelp() {
        System.out.println("\n=== 사용 가능한 명령어 ===");
        System.out.println("comic-add            : 만화책 등록");
        System.out.println("comic-list           : 만화책 목록");
        System.out.println("comic-detail [id]    : 만화책 상세보기");
        System.out.println("comic-update [id]    : 만화책 수정");
        System.out.println("comic-delete [id]    : 만화책 삭제");
        System.out.println("member-add           : 회원 등록");
        System.out.println("member-list          : 회원 목록");
        System.out.println("rent [comicId] [memberId] : 대여");
        System.out.println("return [rentalId]    : 반납");
        System.out.println("rental-list          : 대여 목록 (전체)");
        System.out.println("rental-list open     : 대여 목록 (미반납만)");
        System.out.println("help                 : 도움말");
        System.out.println("exit                 : 종료\n");
    }
    
    // ========== 만화책 관련 기능 ==========
    
    /**
     * 만화책 등록
     */
    private void addComic() {
        System.out.print("제목: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("제목은 필수입니다.");
            return;
        }
        
        System.out.print("권수: ");
        String volumeStr = scanner.nextLine().trim();
        int volume;
        
        try {
            volume = Integer.parseInt(volumeStr);
            if (volume <= 0) {
                System.out.println("권수는 1 이상의 숫자여야 합니다.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
            return;
        }
        
        System.out.print("작가: ");
        String author = scanner.nextLine().trim();
        
        if (author.isEmpty()) {
            System.out.println("작가는 필수입니다.");
            return;
        }
        
        Comic comic = new Comic(title, volume, author);
        int id = comicRepository.addComic(comic);
        
        if (id > 0) {
            System.out.println("=> 만화책이 등록되었습니다. (id=" + id + ")");
        } else {
            System.out.println("만화책 등록에 실패했습니다.");
        }
    }
    
    /**
     * 만화책 목록 출력
     */
    private void listComics() {
        List<Comic> comics = comicRepository.getAllComics();
        
        if (comics.isEmpty()) {
            System.out.println("등록된 만화책이 없습니다.");
            return;
        }
        
        System.out.println("\n번호 | 제목            | 권수 | 작가                 | 상태       | 등록일");
        System.out.println("--------------------------------------------------------------------");
        
        for (Comic comic : comics) {
            System.out.println(comic.toString());
        }
        
        System.out.println("\n총 " + comics.size() + "권의 만화책이 등록되어 있습니다.");
    }
    
    /**
     * 만화책 상세보기
     */
    private void showComicDetail(Rq rq) {
        Integer id = rq.getParamAsInt(0);
        
        if (id == null) {
            System.out.println("사용법: comic-detail [id]");
            return;
        }
        
        Comic comic = comicRepository.getComicById(id);
        
        if (comic == null) {
            System.out.println("해당 ID의 만화책을 찾을 수 없습니다. (id=" + id + ")");
            return;
        }
        
        System.out.println("\n=== 만화책 상세 정보 ===");
        System.out.println("ID: " + comic.getId());
        System.out.println("제목: " + comic.getTitle());
        System.out.println("권수: " + comic.getVolume() + "권");
        System.out.println("작가: " + comic.getAuthor());
        System.out.println("상태: " + (comic.isRented() ? "대여중" : "대여가능"));
        System.out.println("등록일: " + comic.getRegDate());
    }
    
    /**
     * 만화책 수정
     */
    private void updateComic(Rq rq) {
        Integer id = rq.getParamAsInt(0);
        
        if (id == null) {
            System.out.println("사용법: comic-update [id]");
            return;
        }
        
        Comic comic = comicRepository.getComicById(id);
        
        if (comic == null) {
            System.out.println("해당 ID의 만화책을 찾을 수 없습니다. (id=" + id + ")");
            return;
        }
        
        System.out.println("현재 정보: " + comic.getTitle() + " " + comic.getVolume() + "권 - " + comic.getAuthor());
        
        System.out.print("새 제목 (엔터=변경안함): ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) {
            comic.setTitle(title);
        }
        
        System.out.print("새 권수 (엔터=변경안함): ");
        String volumeStr = scanner.nextLine().trim();
        if (!volumeStr.isEmpty()) {
            try {
                int volume = Integer.parseInt(volumeStr);
                if (volume > 0) {
                    comic.setVolume(volume);
                } else {
                    System.out.println("권수는 1 이상이어야 합니다.");
                }
            } catch (NumberFormatException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
            }
        }
        
        System.out.print("새 작가 (엔터=변경안함): ");
        String author = scanner.nextLine().trim();
        if (!author.isEmpty()) {
            comic.setAuthor(author);
        }
        
        if (comicRepository.updateComic(comic)) {
            System.out.println("=> 만화책 정보가 수정되었습니다.");
        } else {
            System.out.println("만화책 수정에 실패했습니다.");
        }
    }
    
    /**
     * 만화책 삭제
     */
    private void deleteComic(Rq rq) {
        Integer id = rq.getParamAsInt(0);
        
        if (id == null) {
            System.out.println("사용법: comic-delete [id]");
            return;
        }
        
        Comic comic = comicRepository.getComicById(id);
        
        if (comic == null) {
            System.out.println("해당 ID의 만화책을 찾을 수 없습니다. (id=" + id + ")");
            return;
        }
        
        System.out.println("삭제할 만화책: " + comic.getTitle() + " " + comic.getVolume() + "권 - " + comic.getAuthor());
        System.out.print("정말 삭제하시겠습니까? (y/N): ");
        String confirm = scanner.nextLine().trim();
        
        if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
            System.out.println("삭제가 취소되었습니다.");
            return;
        }
        
        if (comicRepository.deleteComic(id)) {
            System.out.println("=> 만화책이 삭제되었습니다.");
        } else {
            System.out.println("만화책 삭제에 실패했습니다. (대여 기록이 있는 경우 삭제할 수 없습니다)");
        }
    }
    
    // ========== 회원 관련 기능 ==========
    
    /**
     * 회원 등록
     */
    private void addMember() {
        System.out.print("이름: ");
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty()) {
            System.out.println("이름은 필수입니다.");
            return;
        }
        
        System.out.print("전화번호 (선택사항): ");
        String phone = scanner.nextLine().trim();
        
        if (phone.isEmpty()) {
            phone = null;
        }
        
        Member member = new Member(name, phone);
        int id = memberRepository.addMember(member);
        
        if (id > 0) {
            System.out.println("=> 회원이 등록되었습니다. (id=" + id + ")");
        } else {
            System.out.println("회원 등록에 실패했습니다.");
        }
    }
    
    /**
     * 회원 목록 출력
     */
    private void listMembers() {
        List<Member> members = memberRepository.getAllMembers();
        
        if (members.isEmpty()) {
            System.out.println("등록된 회원이 없습니다.");
            return;
        }
        
        System.out.println("\n번호 | 이름       | 전화번호          | 등록일");
        System.out.println("------------------------------------------------");
        
        for (Member member : members) {
            System.out.println(member.toString());
        }
        
        System.out.println("\n총 " + members.size() + "명의 회원이 등록되어 있습니다.");
    }
    
    // ========== 대여/반납 관련 기능 ==========
    
    /**
     * 대여 처리
     */
    private void rentComic(Rq rq) {
        Integer comicId = rq.getParamAsInt(0);
        Integer memberId = rq.getParamAsInt(1);
        
        if (comicId == null || memberId == null) {
            System.out.println("사용법: rent [comicId] [memberId]");
            return;
        }
        
        // 만화책 존재 여부 확인
        Comic comic = comicRepository.getComicById(comicId);
        if (comic == null) {
            System.out.println("존재하지 않는 만화책입니다. (id=" + comicId + ")");
            return;
        }
        
        // 회원 존재 여부 확인
        Member member = memberRepository.getMemberById(memberId);
        if (member == null) {
            System.out.println("존재하지 않는 회원입니다. (id=" + memberId + ")");
            return;
        }
        
        System.out.println("대여할 만화책: " + comic.getTitle() + " " + comic.getVolume() + "권");
        System.out.println("대여자: " + member.getName());
        
        int rentalId = rentalRepository.rentComic(comicId, memberId);
        
        if (rentalId > 0) {
            System.out.println("=> 대여 완료: [대여id=" + rentalId + "] 만화(" + comicId + ") → 회원(" + memberId + ")");
        } else {
            System.out.println("대여에 실패했습니다.");
        }
    }
    
    /**
     * 반납 처리
     */
    private void returnComic(Rq rq) {
        Integer rentalId = rq.getParamAsInt(0);
        
        if (rentalId == null) {
            System.out.println("사용법: return [rentalId]");
            return;
        }
        
        if (rentalRepository.returnComic(rentalId)) {
            System.out.println("=> 반납 완료: 대여id=" + rentalId);
        } else {
            System.out.println("반납에 실패했습니다.");
        }
    }
    
    /**
     * 대여 목록 출력
     */
    private void listRentals(Rq rq) {
        String option = rq.getParam(0);
        List<Rental> rentals;
        
        if ("open".equals(option)) {
            rentals = rentalRepository.getUnreturnedRentals();
            System.out.println("\n=== 미반납 대여 목록 ===");
        } else {
            rentals = rentalRepository.getAllRentals();
            System.out.println("\n=== 전체 대여 목록 ===");
        }
        
        if (rentals.isEmpty()) {
            System.out.println("대여 기록이 없습니다.");
            return;
        }
        
        System.out.println("대여id | 만화책            | 회원       | 대여일     | 반납일");
        System.out.println("----------------------------------------------------------");
        
        for (Rental rental : rentals) {
            System.out.println(rental.toString());
        }
        
        System.out.println("\n총 " + rentals.size() + "건의 대여 기록이 있습니다.");
    }
}
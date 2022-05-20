import {Component, OnInit} from '@angular/core';
import {LocatorService} from "../locator.service";
import {ActivatedRoute} from "@angular/router";
import {Report} from "../report";

@Component({
  selector: 'app-beacon-detail',
  templateUrl: './beacon-detail.component.html',
  styleUrls: ['./beacon-detail.component.scss']
})
export class BeaconDetailComponent implements OnInit {
  token: string = "";
  publicInfo: string[] = [];
  reports: Report[] = [];

  constructor(private locatorService: LocatorService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.getBeacon();
  }

  getBeacon(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.locatorService.getPublic(id).subscribe(publicInfo => this.publicInfo = publicInfo);
      if (this.token) {
        this.locatorService.getPrivate(id, this.token).subscribe(reports => this.reports = reports);
      }
    }
  }

}
